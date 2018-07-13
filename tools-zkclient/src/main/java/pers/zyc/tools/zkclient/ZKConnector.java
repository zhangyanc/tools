package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.utils.GeneralThreadFactory;
import pers.zyc.tools.utils.event.Listenable;
import pers.zyc.tools.utils.event.MulticastExceptionHandler;
import pers.zyc.tools.utils.event.Multicaster;
import pers.zyc.tools.utils.lifecycle.ThreadService;
import pers.zyc.tools.zkclient.listener.ConnectionListener;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import static org.apache.zookeeper.Watcher.Event.KeeperState.Disconnected;
import static org.apache.zookeeper.Watcher.Event.KeeperState.SyncConnected;

/**
 * ZooKeeper连接器(自动重连、事件发布)
 *
 * @author zhangyancheng
 */
final class ZKConnector extends ThreadService implements Watcher, Listenable<ConnectionListener> {
	private final static Logger LOGGER = LoggerFactory.getLogger(ZKConnector.class);

	/**
	 * 监听器回调异常处理器
	 */
	private final static MulticastExceptionHandler EXCEPTION_HANDLER = new LogMulticastExceptionHandler(LOGGER);

	/**
	 * 连接事件广播器, listener回调的阻塞将影响connector线程对新连接事件的处理
	 */
	private final Multicaster<ConnectionListener> multicaster = new Multicaster<ConnectionListener>() {
		{
			setExceptionHandler(EXCEPTION_HANDLER);
		}
	};


	/**
	 * 连接事件锁, 用于ZK连接事件的等待和唤醒
	 */
	private final Condition incomeEventCondition = serviceLock.newCondition();

	/**
	 * 协商后的sessionTimeout, 期望连接事件到达的超时时间
	 */
	private int eventWaitTimeout;
	/**
	 * session id用来判断是否同一个ZooKeeper实例重连成功
	 */
	private long zooKeeperSessionId;

	/**
	 * 当前事件, 新入事件(与当前事件比较判断连接状态)
	 */
	private Watcher.Event.KeeperState currentState, incomeState;

	/**
	 * ZooKeeper实例
	 */
	private ZooKeeper zooKeeper;

	private final String connectStr;
	private final int sessionTimeout;

	/**
	 * @param connectStr 连接地址
	 * @param sessionTimeout 会话超时时间
	 */
	ZKConnector(String connectStr, int sessionTimeout) {
		this.connectStr = connectStr;
		this.sessionTimeout = sessionTimeout;
		this.eventWaitTimeout = sessionTimeout;

		GeneralThreadFactory threadFactory = new GeneralThreadFactory(getName());
		threadFactory.setExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				LOGGER.error("Uncaught exception", e);
				stop();
			}
		});
		setThreadFactory(threadFactory);
	}

	@Override
	public void addListener(ConnectionListener connectionListener) {
		multicaster.addListener(connectionListener);
	}

	@Override
	public void removeListener(ConnectionListener connectionListener) {
		multicaster.removeListener(connectionListener);
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();
		closeZooKeeper();
		multicaster.removeAllListeners();
	}

	@Override
	public void process(WatchedEvent event) {
		//只处理连接事件
		if (event.getPath() == null) {
			serviceLock.lock();
			try {
				currentState = incomeState = event.getState();
				LOGGER.debug("Watched connection event[{}]",  incomeState);

				incomeEventCondition.signal();
			} finally {
				serviceLock.unlock();
			}
		}
	}

	@Override
	protected ServiceRunnable getRunnable() {
		return new ServiceRunnable() {

			@Override
			protected long getInterval() {
				return 0;
			}

			@Override
			protected void execute() throws InterruptedException {
				execute0();
			}
		};
	}

	private void execute0() throws InterruptedException {
		Publisher publisher;

		serviceLock.lockInterruptibly();
		try {
			if (!isRunning()) {
				return;
			}

			//初始以及会话关闭后的下轮, 创建ZooKeeper
			if (zooKeeper == null) {
				try {
					zooKeeper = new ZooKeeper(connectStr, sessionTimeout, this);
				} catch (IOException e) {
					throw new RuntimeException("Creating ZooKeeper instance error!", e);
				}
			}

			long timeout = TimeUnit.MILLISECONDS.toNanos(eventWaitTimeout);
			while (this.incomeState == null && timeout > 0) {
				//等待新事件
				timeout = incomeEventCondition.awaitNanos(timeout);
			}
			final Watcher.Event.KeeperState incomeState = this.incomeState;
			//清空incomeState(如果有的话), 表示此轮已经消费
			this.incomeState = null;

			/*
			 * 状态判断分以下情况:
			 *      1. 未收到事件且当前状态为空, 表示当前ZooKeeper尚未连通过, 无需发布事件继续等待连接
			 *      1. 未收到事件且当前处于连通状态, 表示正常连通无需发布事件
			 *      2. 收到SyncConnected事件, 判断是否是同一个ZooKeeper实例重连成功, 发布connected并指示是否为新会话
			 *      3. 收到Disconnected事件, 发布Disconnected事件, 然后等待ZooKeeper的自动重连
			 *      4. 其他均为错误(未支持)状态, 主动关闭ZooKeeper, 发布会话Disconnected事件, 并指示session 关闭
			 */
			if (incomeState == null &&
			   (currentState == null || currentState == SyncConnected)) {
				return;
			}
			LOGGER.info("Publish incoming event[{}], session id: 0x{}",
					incomeState, Long.toHexString(zooKeeperSessionId));

			publisher = new Publisher() {

				@Override
				public void publish() throws InterruptedException {
					if (incomeState == SyncConnected) {
						boolean newSession = zooKeeperSessionId != zooKeeper.getSessionId();
						if (newSession) {
							//更新协商后真正的超时时间
							eventWaitTimeout = zooKeeper.getSessionTimeout();
							//保存新会话id, 用于收到SyncConnected后判断是否为新会话
							zooKeeperSessionId = zooKeeper.getSessionId();
						}
						multicaster.listeners.onConnected(newSession);
					} else {
						/*
						 * 断连后只需要等待自动重连,
						 * 如果下轮未收到事件(SyncConnected), 则在ZooKeeper Server端实际上已经会话超时了
						 */
						boolean sessionClosed = incomeState != Disconnected;
						if (sessionClosed) {
							closeZooKeeper();
						}
						multicaster.listeners.onDisconnected(sessionClosed);
					}
				}
			};
		} finally {
			serviceLock.unlock();
		}

		//发布事件
		publisher.publish();
	}

	/**
	 * @return 是否连通ZooKeeper Server
	 */
	boolean isConnected() {
		serviceLock.lock();
		try {
			return isRunning() && currentState == SyncConnected;
		} finally {
			serviceLock.unlock();
		}
	}

	/**
	 * @return 当前ZooKeeper实例
	 */
	ZooKeeper getZooKeeper() {
		serviceLock.lock();
		try {
			return zooKeeper;
		} finally {
			serviceLock.unlock();
		}
	}

	interface Publisher {
		void publish() throws InterruptedException;
	}

	/**
	 * 关闭ZooKeeper及自身的重连机制
	 */
	private void closeZooKeeper() throws InterruptedException {
		if (zooKeeper != null) {
			LOGGER.info("Closing zookeeper: {}", zooKeeper);
			try {
				zooKeeper.close();
			} catch (InterruptedException interrupted) {
				throw interrupted;
			} catch (Exception ignore) {
			} finally {
				zooKeeper = null;
				zooKeeperSessionId = 0;
				currentState = incomeState = null;
				eventWaitTimeout = sessionTimeout;
			}
		}
	}
}
