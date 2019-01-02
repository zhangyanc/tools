package pers.zyc.tools.zkclient;

import org.apache.zookeeper.*;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.utils.event.Listenable;
import pers.zyc.tools.utils.event.MulticastExceptionHandler;
import pers.zyc.tools.utils.event.Multicaster;
import pers.zyc.tools.utils.retry.*;
import pers.zyc.tools.zkclient.listener.ClientDestroyListener;
import pers.zyc.tools.zkclient.listener.ConnectionListener;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.zookeeper.KeeperException.*;

/**
 * @author zhangyancheng
 */
public class ZKClient implements ZooKeeperOperations, Listenable<ClientDestroyListener> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ZKClient.class);
	/**
	 * 单字节魔数, 节点添加了身份数据的标志
	 */
	private static final byte MAGIC_BYTE = '^';

	/**
	 * 异常处理器, 所有事件发布异常都将记录日志
	 */
	private static final MulticastExceptionHandler EXCEPTION_HANDLER = new LogMulticastExceptionHandler(LOGGER);
	private final Multicaster<ClientDestroyListener> multicaster = new Multicaster<ClientDestroyListener>() {
		{
			setExceptionHandler(EXCEPTION_HANDLER);
		}
	};

	/**
	 * client id(uuid), 用于请求重试的幂等性保证
	 */
	private final String id;
	private final byte[] idBytes;

	/**
	 * zookeeper操作的重试策略(根据构造方法传入的重试次数和间隔构造), 为null表示不重试
	 */
	private final BaseRetryPolicy retryPolicy;

	/**
	 * 请求id记录, 用于请求重试的幂等性保证
	 */
	private final AtomicInteger idempotentRequestId = new AtomicInteger();

	/**
	 * 连接器, 连接状态维护以及发布状态变更事件
	 */
	private final ZKConnector connector;

	/**
	 * 销毁标志
	 */
	private final AtomicBoolean destroyed = new AtomicBoolean();

	public ZKClient(String connectStr, int sessionTimeout) {
		this(connectStr, sessionTimeout, 0, 0);
	}

	/**
	 * 创建ZKClient, 返构造方法返回已启动连接zookeeper, 不保证已经连通(这点与原生的ZooKeeper构造一样)
	 * 如果要等待连接, 可返回后调用waitToConnected
	 *
	 * @param connectStr 连接地址
	 * @param sessionTimeout 会话超时时间
	 * @param retryTimes zookeeper请求重试次数, 请求总数 <= 重试次数 + 1
	 * @param retryPerWaitTimeout 单次重试的等待超时时间, 超时后不再继续重试
	 */
	public ZKClient(String connectStr, int sessionTimeout, int retryTimes, int retryPerWaitTimeout) {
		connector = new ZKConnector(connectStr, sessionTimeout);
		connector.start();

		if (retryTimes <= 0) {
			//不进行重试
			retryPolicy = null;
		} else {
			if (retryPerWaitTimeout <= 0) {
				throw new IllegalArgumentException("retryPerWaitTimeout: " + retryPerWaitTimeout + " <= 0!");
			}

			//重试策略条件, 连通时可快速恢复重试, 无需retryPerWaitTimeout结束
			ConnectedRetryCondition connectedRetryCondition = new ConnectedRetryCondition();
			addConnectionListener(connectedRetryCondition);

			//条件重试策略, 只在zookeeper连接异常时才进行重试
			retryPolicy = new ConditionalRetryPolicy(connectedRetryCondition) {

				@Override
				public Boolean handleException(Throwable cause, Callable<?> callable) {
					LOGGER.warn("Retry call exception: {}", cause.getMessage());

					return cause instanceof ConnectionLossException ||
						   cause instanceof OperationTimeoutException ||
						   cause instanceof SessionExpiredException ||
						   cause instanceof SessionMovedException;
				}
			};
			retryPolicy.setMaxRetryTimes(retryTimes);
			retryPolicy.setRetryDelay(retryPerWaitTimeout);
		}

		//生成id
		SecureRandom random = new SecureRandom();
		idBytes = new byte[16];
		random.nextBytes(idBytes);

		ByteBuffer buf = ByteBuffer.wrap(idBytes);
		id = new UUID(buf.getLong(), buf.getLong()).toString();
	}

	private class ConnectedRetryCondition implements RetryCondition, ConnectionListener {

		@Override
		public boolean check() {
			//重试检查条件, 如果不成立不再继续重试
			return isConnected();
		}

		@Override
		public Object getMutex() {
			//所有重试线程都在此对象(monitor)上等待
			return this;
		}

		@Override
		public void onConnected(boolean newSession) {
			//连通后唤醒所有重试等待线程
			synchronized (this) {
				notifyAll();
			}
		}

		@Override
		public void onDisconnected(boolean sessionClosed) {
		}
	}

	@Override
	public void addListener(ClientDestroyListener listener) {
		multicaster.addListener(listener);
	}

	@Override
	public void removeListener(ClientDestroyListener listener) {
		multicaster.removeListener(listener);
	}

	/**
	 * 销毁客户端
	 */
	public void destroy() {
		if (destroyed.compareAndSet(false, true)) {
			multicaster.listeners.onDestroy();
			multicaster.removeAllListeners();
			connector.stop();
		}
	}

	/**
	 * 等待连通直到超时
	 *
	 * @param timeout 超时
	 * @param timeUnit 单位
	 * @return zookeeper是否连通
	 * @throws InterruptedException 线程被中断
	 */
	public boolean waitToConnected(long timeout, TimeUnit timeUnit) throws InterruptedException {
		if (isConnected()) {
			return true;
		}
		final CountDownLatch connectedLatch = new CountDownLatch(1);
		ConnectionListener waitListener = new ConnectionListener() {

			@Override
			public void onConnected(boolean newSession) {
				connectedLatch.countDown();
			}

			@Override
			public void onDisconnected(boolean sessionClosed) {
			}
		};
		addConnectionListener(waitListener);
		try {
			return isConnected() || connectedLatch.await(timeout, timeUnit);
		} finally {
			removeConnectionListener(waitListener);
		}
	}

	ZooKeeper getZooKeeper() {
		return connector.getZooKeeper();
	}

	/**
	 * @return 客户端id
	 */
	public String getClientId() {
		return this.id;
	}

	/**
	 * @return 是否连通ZooKeeper Server
	 */
	public boolean isConnected() {
		return connector.isConnected();
	}

	/**
	 * 添加连接监听器, 连接状态变更后触发回调
	 *
	 * @param connectionListener 连接监听器
	 */
	public void addConnectionListener(ConnectionListener connectionListener) {
		connector.addListener(connectionListener);
	}

	/**
	 * 移除连接监听器, 不再触发连接事件回调
	 *
	 * @param connectionListener 连接监听器
	 */
	public void removeConnectionListener(ConnectionListener connectionListener) {
		connector.removeListener(connectionListener);
	}

	/**
	 * 获取节点事件观察器, 如果不存在则新建
	 *
	 * @param nodePath 节点路径
	 * @return 事件观察器
	 */
	public NodeEventWatcher createNodeEventWatcher(String nodePath) {
		return new NodeEventDurableWatcher(nodePath, this);
	}

	/**
	 * 获取给定节点上的选举器, 如果不存在则新建
	 *
	 * @param electionPath 选举节点
	 *                     1. 节点必须符合zookeeper path格式,且不能是临时节点
	 *                     2. 选举节点必须存在, 且不能被删除, 否则选举将发生错误
	 * @return 选举器
	 */
	public Election createElection(String electionPath) {
		return createElection(electionPath, Elector.Mode.FOLLOWER, new byte[0]);
	}

	public Election createElection(String electionPath, Elector.Mode mode, byte[] memberData) {
		return new LeaderElection(electionPath, this, mode, memberData);
	}

	/**
	 * 移除唯一性数据
	 */
	private byte[] removeIdentityData(byte[] data) {
		if (data.length == 0 || data[0] != MAGIC_BYTE) {
			return data;
		}
		byte[] realData = new byte[data.length - 21];
		ByteBuffer buf = ByteBuffer.wrap(data, 21, realData.length);
		buf.get(realData);
		return realData;
	}

	/**
	 * create、setData请求时设置请求唯一性数据
	 */
	private byte[] appendIdentityData(byte[] data, int rId) {
		byte[] appendedData = new byte[21 + data.length];
		ByteBuffer buf = ByteBuffer.wrap(appendedData);
		buf.put(MAGIC_BYTE);//1 字节魔数
		buf.put(idBytes);	//16字节id
		buf.putInt(rId);	//4 字节请求id
		buf.put(data);
		return appendedData;
	}

	private <V> V doRetry(Callable<V> request) throws KeeperException, InterruptedException {
		try {
			//重试执行
			return RetryLoop.execute(request, retryPolicy);
		} catch (RetryFailedException retryFailed) {
			LOGGER.warn("Retry failed, {}", retryFailed.getRetryStat());
			//抛出重试操作的原始异常
			Throwable retryFailCause = retryFailed.getCause();
			if (retryFailCause instanceof KeeperException) {
				throw (KeeperException) retryFailCause;
			}
			throw new ClientException("Retry failed", retryFailCause);
		}
	}

	private abstract class IdempotentRequest<V> implements Callable<V> {
		final int rId = idempotentRequestId.incrementAndGet();

		boolean retry = false;

		@Override
		public V call() throws Exception {
			try {
				return doRequest();
			} finally {
				retry = true;
			}
		}

		protected abstract V doRequest() throws Exception;
	}

	@Override
	public String create(final String path, final byte[] data, final CreateMode createMode) throws
			KeeperException, InterruptedException {

		if (retryPolicy == null) {
			return getZooKeeper().create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
		}

		return doRetry(new IdempotentRequest<String>() {

			final byte[] newData = appendIdentityData(data, rId);
			final String newPath = createMode.isSequential() ? path + id : path;

			@Override
			protected String doRequest() throws Exception {
				boolean isRetry = retry;
				try {
					if (createMode.isSequential() && isRetry) {
						//顺序节点不会报NodeExists, 非首次创建必须检查是否已经创建成功, 否则可能创建多个顺序节点
						int lastSlashIndex = newPath.lastIndexOf("/");
						String parent = newPath.substring(0, lastSlashIndex);
						String node = newPath.substring(lastSlashIndex + 1);
						List<String> children = getZooKeeper().getChildren(parent, false);
						for (String child : children) {
							//node中包含当前请求唯一身份字符串, 如果匹配到就认为之前操作已经创建成功
							if (child.startsWith(node)) {
								return parent + "/" + child;
							}
						}
					}
					return getZooKeeper().create(newPath, newData, ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
				} catch (KeeperException.NodeExistsException e) {
					if (isRetry) {
						//非顺序节点重试后报NodeExists, 检查数据是否为当前请求创建成功的
						byte[] existsData = getZooKeeper().getData(path, false, null);
						if (Arrays.equals(existsData, newData)) {
							//已经成功创建非顺序节点
							return path;
						}
					}
					throw e;
				}
			}
		});
	}

	@Override
	public void delete(String path) throws KeeperException, InterruptedException {
		delete(path, -1);
	}

	@Override
	public void delete(final String path, final int version) throws KeeperException, InterruptedException {
		if (retryPolicy == null) {
			getZooKeeper().delete(path, version);
			return;
		}

		doRetry(new IdempotentRequest<Void>() {

			@Override
			protected Void doRequest() throws Exception {
				boolean isRetry = retry;
				try {
					getZooKeeper().delete(path, version);
					return null;
				} catch (KeeperException.NoNodeException e) {
					//非重试过程中的NoNode直接抛出
					if (!isRetry) {
						throw e;
					}
					//重试过程无法判断是否当前请求删除成功, 不抛出异常
					return null;
				}
			}
		});
	}

	@Override
	public boolean exists(String path) throws KeeperException, InterruptedException {
		return exists(path, null) != null;
	}

	@Override
	public Stat exists(final String path, final Watcher watcher) throws KeeperException, InterruptedException {
		if (retryPolicy == null) {
			return getZooKeeper().exists(path, watcher);
		}

		return doRetry(new Callable<Stat>() {

			@Override
			public Stat call() throws Exception {
				return getZooKeeper().exists(path, watcher);
			}
		});
	}

	@Override
	public byte[] getData(String path) throws KeeperException, InterruptedException {
		return getData(path, null, null);
	}

	@Override
	public byte[] getData(String path, Stat stat) throws KeeperException, InterruptedException {
		return getData(path, null, stat);
	}

	@Override
	public byte[] getData(String path, Watcher watcher) throws KeeperException, InterruptedException {
		return getData(path, watcher, null);
	}

	@Override
	public byte[] getData(final String path, final Watcher watcher, final Stat stat) throws
			KeeperException, InterruptedException {

		if (retryPolicy == null) {
			byte[] data = getZooKeeper().getData(path, watcher, stat);
			return removeIdentityData(data);
		}

		return doRetry(new Callable<byte[]>() {

			@Override
			public byte[] call() throws Exception {
				byte[] data = getZooKeeper().getData(path, watcher, stat);
				return removeIdentityData(data);
			}
		});
	}

	@Override
	public Stat setData(final String path, final byte[] data, final int version) throws
			KeeperException, InterruptedException {

		if (retryPolicy == null) {
			return getZooKeeper().setData(path, data, version);
		}

		return doRetry(new IdempotentRequest<Stat>() {

			final byte[] newData = appendIdentityData(data, rId);

			@Override
			protected Stat doRequest() throws Exception {
				boolean isRetry = retry;
				try {
					return getZooKeeper().setData(path, newData, version);
				} catch (KeeperException.BadVersionException e) {
					if (isRetry) {
						//和创建非顺序节点逻辑一样, 检查是否当前请求set成功
						Stat stat = new Stat();
						byte[] existsData = getZooKeeper().getData(path, false, stat);
						if (Arrays.equals(existsData, newData)) {
							return stat;
						}
					}
					throw e;
				}
			}
		});
	}

	@Override
	public List<String> getChildren(String path) throws KeeperException, InterruptedException {
		return getChildren(path, null);
	}

	@Override
	public List<String> getChildren(final String path, final Watcher watcher) throws
			KeeperException, InterruptedException {

		if (retryPolicy == null) {
			return getZooKeeper().getChildren(path, watcher);
		}

		return doRetry(new Callable<List<String>>() {

			@Override
			public List<String> call() throws Exception {
				return getZooKeeper().getChildren(path, watcher);
			}
		});
	}

	@Override
	public List<OpResult> multi(Iterable<Op> ops) throws InterruptedException, KeeperException {
		//TODO multi
		return null;
	}
}
