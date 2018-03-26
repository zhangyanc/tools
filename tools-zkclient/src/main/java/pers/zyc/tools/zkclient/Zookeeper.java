package pers.zyc.tools.zkclient;

import org.apache.zookeeper.AsyncCallback.*;
import org.apache.zookeeper.*;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.retry.*;
import pers.zyc.tools.zkclient.listener.ConnectionListenerAdapter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import static org.apache.zookeeper.KeeperException.*;

/**
 * @author zhangyancheng
 */
class Zookeeper implements IZookeeper, InvocationHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(Zookeeper.class);

	/**
	 * 原生ZooKeeper实例, ZKClient所有的对ZooKeeper的操作都由此完成
	 */
	private ZooKeeper zooKeeper;
	/**
	 * 提供读写锁控制, 在操作ZooKeeper时禁止关闭客户端
	 */
	private final ZKClient zkClient;
	/**
	 * 重试策略
	 */
	private BaseRetryPolicy retryPolicy;

	Zookeeper(ZKClient zkClient) {
		this.zkClient = zkClient;
		//添加连接监听器, 用于连通时唤醒可能存在的重试等待线程
		zkClient.addListener(new ConnectionListener());

		ClientConfig config = zkClient.getConfig();
		if (config.isUseRetry()) {
			retryPolicy = new AwaitConnectedRetryPolicy(new ConnectedRetryCondition());
			retryPolicy.setMaxRetryTimes(config.getRetryTimes());
			retryPolicy.setRetryDelay(config.getRetryPerWaitTimeout());
		}
	}

	@Override
	public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
		zkClient.readLock.lock();
		try {
			if (!zkClient.isConnected()) {
				throw new ClientException("ZooKeeper server is not connected!");
			}
			return RetryLoop.execute(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					return method.invoke(Zookeeper.this, args);
				}
			}, retryPolicy);
		} catch (InterruptedException interrupted) {
			throw interrupted;
		} catch (RetryFailedException retryFailed) {
			LOGGER.error("Retry failed, {}", retryFailed.getRetryStat());
			Throwable relCause = retryFailed.getCause();
			if (relCause instanceof InvocationTargetException) {
				//抛出ZooKeeper实例调用异常
				throw ((InvocationTargetException) relCause).getTargetException();
			}
			throw new ClientException(relCause);
		} catch (Throwable cause) {
			LOGGER.error("Unexpected error", cause);
			throw new ClientException(cause);
		} finally {
			zkClient.readLock.unlock();
		}
	}

	/**
	 * 连接事件监听器, 当连接或者重连成功时更新ZooKeeper并唤醒重试等待线程
	 */
	private class ConnectionListener extends ConnectionListenerAdapter {

		@Override
		public void onConnected(boolean newSession) {
			if (newSession) {
				zooKeeper = zkClient.getZooKeeper();
			}
			synchronized (Zookeeper.this) {
				//唤醒所有的重试等待线程
				Zookeeper.this.notifyAll();
			}
		}
	}

	/**
	 * 重试检查条件, 检查是否连接成功
	 */
	private class ConnectedRetryCondition implements RetryCondition {

		@Override
		public boolean check() {
			return zkClient.isConnected();
		}

		@Override
		public Object getMutex() {
			return Zookeeper.this;
		}
	}

	/**
	 * 重试策略, 等待连接成功后重试
	 */
	private class AwaitConnectedRetryPolicy extends ConditionalRetryPolicy {

		AwaitConnectedRetryPolicy(RetryCondition retryCondition) {
			super(retryCondition);
		}

		@Override
		public Boolean handleException(Throwable cause, Callable<?> callable) {
			LOGGER.error("Call error!", cause);
			/*
			 * retry callable通过反射调用zookeeper, 需要判断zookeeper api调用异常.
			 * 当且仅当发生了KeeperException且为连接异常才进行重试
			 */
			if (cause instanceof InvocationTargetException) {
				Throwable throwable = ((InvocationTargetException) cause).getTargetException();
				return throwable instanceof KeeperException &&
							   (throwable instanceof ConnectionLossException ||
								throwable instanceof OperationTimeoutException ||
								throwable instanceof SessionExpiredException ||
								throwable instanceof SessionMovedException);
			}
			return false;
		}
	}

	@Override
	public String createPersistent(String path, byte[] data, boolean sequential) throws
			KeeperException, InterruptedException {

		return zooKeeper.create(path, Objects.requireNonNull(data),
				ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.fromFlag(sequential ? 2 : 0));
	}

	@Override
	public String createEphemeral(String path, byte[] data, boolean sequential) throws
			KeeperException, InterruptedException {

		return zooKeeper.create(path, Objects.requireNonNull(data),
				ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.fromFlag(sequential ? 3 : 1));
	}

	@Override
	public String createLive(String path, byte[] data, boolean sequential) throws
			KeeperException, InterruptedException {

		return zooKeeper.create(path, Objects.requireNonNull(data),
				ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.fromFlag(sequential ? 3 : 1));
	}

	@Override
	public void delete(String path) throws KeeperException, InterruptedException {
		delete(path, -1);
	}

	@Override
	public void delete(String path, int version) throws KeeperException, InterruptedException {
		zooKeeper.delete(path, version);
	}

	@Override
	public void delete(String path, int version, VoidCallback cb, Object ctx) {
		zooKeeper.delete(path, version, cb, ctx);
	}

	@Override
	public boolean exists(String path) throws KeeperException, InterruptedException {
		return exists(path, null) != null;
	}

	@Override
	public Stat exists(String path, Watcher watcher) throws KeeperException, InterruptedException {
		return zooKeeper.exists(path, watcher);
	}

	@Override
	public void exists(String path, Watcher watcher, StatCallback cb, Object ctx) {
		zooKeeper.exists(path, watcher, cb, ctx);
	}

	@Override
	public byte[] getData(String path) throws KeeperException, InterruptedException {
		return getData(path, null);
	}

	@Override
	public byte[] getData(String path, Watcher watcher) throws KeeperException, InterruptedException {
		return zooKeeper.getData(path, watcher, null);
	}

	@Override
	public byte[] getData(String path, Watcher watcher, Stat stat) throws KeeperException, InterruptedException {
		return zooKeeper.getData(path, watcher, stat);
	}

	@Override
	public void getData(String path, Watcher watcher, DataCallback cb, Object ctx) {
		zooKeeper.getData(path, watcher, cb, ctx);
	}

	@Override
	public Stat setData(String path, byte[] data, int version) throws KeeperException, InterruptedException {
		return zooKeeper.setData(path, Objects.requireNonNull(data), version);
	}

	@Override
	public void setData(String path, byte[] data, int version, StatCallback cb, Object ctx) {
		zooKeeper.setData(path, Objects.requireNonNull(data), version, cb, ctx);
	}

	@Override
	public List<String> getChildren(String path) throws KeeperException, InterruptedException {
		return getChildren(path, null);
	}

	@Override
	public List<String> getChildren(String path, Watcher watcher) throws KeeperException, InterruptedException {
		return zooKeeper.getChildren(path, watcher);
	}

	@Override
	public void getChildren(String path, Watcher watcher, ChildrenCallback cb, Object ctx) {
		zooKeeper.getChildren(path, watcher, cb, ctx);
	}

	@Override
	public void getChildren(String path, Watcher watcher, Children2Callback cb, Object ctx) {
		zooKeeper.getChildren(path, watcher, cb, ctx);
	}

	@Override
	public void sync(String path, VoidCallback cb, Object ctx) {
		zooKeeper.sync(path, cb, ctx);
	}

	@Override
	public Transaction transaction() {
		return zooKeeper.transaction();
	}

	@Override
	public List<OpResult> multi(Iterable<Op> ops) throws InterruptedException, KeeperException {
		return zooKeeper.multi(ops);
	}
}
