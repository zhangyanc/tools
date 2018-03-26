package pers.zyc.tools.zkclient;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import pers.zyc.tools.lifecycle.Service;
import pers.zyc.tools.zkclient.listener.*;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author zhangyancheng
 */
public class ZKClient extends Service implements IZookeeper {

	static {
		System.setProperty("zookeeper.disableAutoWatchReset", "true");
	}

	Lock readLock;
	private IZookeeper delegate;
	private ZKConnector connector;
	private LiveNodeReCreator liveNodeReCreator;

	private final ClientConfig config;
	private final ConcurrentMap<String, NodeEventReactor> nodeEventManagers = new ConcurrentHashMap<>();

	public ZKClient(ClientConfig config) {
		this.config = config;
	}

	@Override
	protected Lock initServiceLock() {
		ReadWriteLock rwLock = new ReentrantReadWriteLock();
		this.readLock = rwLock.readLock();
		return rwLock.writeLock();
	}

	@Override
	protected void doStart() {
		connector = new ZKConnector(config.getConnectStr(), config.getSessionTimeout());
		connector.start();

		if (config.isSyncStart()) {
			SyncStartListener syncStartListener = new SyncStartListener();
			connector.addListener(syncStartListener);
			try {
				if (!connector.isConnected()) {
					syncStartListener.latch.await();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				connector.removeListener(syncStartListener);
			}
		}

		delegate = (IZookeeper) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] {IZookeeper.class}, new Zookeeper(this));

		liveNodeReCreator = new LiveNodeReCreator(this);
	}

	@Override
	protected void doStop() throws Exception {
		connector.stop();
	}

	public ClientConfig getConfig() {
		return config;
	}

	private class SyncStartListener extends ConnectionListenerAdapter {

		CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void onConnected(boolean newSession) {
			latch.countDown();
		}
	}

	public ZooKeeper getZooKeeper() {
		return connector.getZooKeeper();
	}

	public boolean isConnected() {
		return connector.isConnected();
	}

	public void addListener(ConnectionListener connectionListener) {
		connector.addListener(connectionListener);
	}

	private NodeEventReactor getNodeEventManager(String path) {
		NodeEventReactor nodeEventReactor = nodeEventManagers.get(path);
		if (nodeEventReactor == null) {
			nodeEventReactor = new NodeEventReactor(path, this);

			NodeEventReactor prev = nodeEventManagers.putIfAbsent(path, nodeEventReactor);
			if (prev != null) {
				nodeEventReactor = prev;
			} else {
				nodeEventReactor.start();
			}
		}
		return nodeEventReactor;
	}

	public void addListener(String path, ExistsEventListener existsEventListener) {
		getNodeEventManager(path).existsReWatcher.addListener(existsEventListener);
	}

	public void addListener(String path, DataEventListener dataEventListener) {
		getNodeEventManager(path).dataReWatcher.addListener(dataEventListener);
	}

	public void addListener(String path, ChildrenListener childrenListener) {
		getNodeEventManager(path).childrenReWatcher.addListener(childrenListener);
	}

	public void removeListener(String path, ExistsEventListener existsEventListener) {
		NodeEventReactor nodeEventReactor = nodeEventManagers.get(path);
		if (nodeEventReactor != null) {
			nodeEventReactor.existsReWatcher.removeListener(existsEventListener);
		}
	}

	public void removeListener(String path, DataEventListener dataEventListener) {
		NodeEventReactor nodeEventReactor = nodeEventManagers.get(path);
		if (nodeEventReactor != null) {
			nodeEventReactor.dataReWatcher.removeListener(dataEventListener);
		}
	}

	public void removeListener(String path, ChildrenListener childrenListener) {
		NodeEventReactor nodeEventReactor = nodeEventManagers.get(path);
		if (nodeEventReactor != null) {
			nodeEventReactor.childrenReWatcher.removeListener(childrenListener);
		}
	}

	@Override
	public String createPersistent(String path, byte[] data, boolean sequential) throws
			KeeperException, InterruptedException {

		return delegate.createPersistent(path, data, sequential);
	}

	@Override
	public String createEphemeral(String path, byte[] data, boolean sequential) throws
			KeeperException, InterruptedException {

		return delegate.createEphemeral(path, data, sequential);
	}

	/**
	 * 添加存活节点, 重建会话时重建此临时节点(节点被手动删除时不会重建)
	 *
	 * @param path 节点路径
	 * @param data 节点数据
	 * @param sequential 是否为顺序节点
	 * @param recreateListener 重建监听器, 如果为null则不发布重建事件
	 * @return actual path
	 * @throws KeeperException ZooKeeper异常
	 * @throws InterruptedException 线程中断
	 */
	public String createLive(String path, byte[] data, boolean sequential, RecreateListener recreateListener) throws
			KeeperException, InterruptedException {

		String retPath = delegate.createEphemeral(path, data, sequential);
		liveNodeReCreator.add(path, data, sequential, recreateListener);
		return retPath;
	}

	public void removeLive(String path) {
		liveNodeReCreator.remove(path);
	}

	@Override
	public void delete(String path) throws KeeperException, InterruptedException {
		delegate.delete(path);
	}

	@Override
	public void delete(String path, int version) throws KeeperException, InterruptedException {
		delegate.delete(path, version);
	}

	@Override
	public void delete(String path, int version, AsyncCallback.VoidCallback cb, Object ctx) {
		delegate.delete(path, version, cb, ctx);
	}

	@Override
	public boolean exists(String path) throws KeeperException, InterruptedException {
		return delegate.exists(path);
	}

	@Override
	public Stat exists(String path, Watcher watcher) throws KeeperException, InterruptedException {
		return delegate.exists(path, watcher);
	}

	@Override
	public void exists(String path, Watcher watcher, AsyncCallback.StatCallback cb, Object ctx) {
		delegate.exists(path, watcher, cb, ctx);
	}

	@Override
	public byte[] getData(String path) throws KeeperException, InterruptedException {
		return delegate.getData(path);
	}

	@Override
	public byte[] getData(String path, Watcher watcher) throws KeeperException, InterruptedException {
		return delegate.getData(path, watcher);
	}

	@Override
	public byte[] getData(String path, Watcher watcher, Stat stat) throws KeeperException, InterruptedException {
		return delegate.getData(path, watcher, stat);
	}

	@Override
	public void getData(String path, Watcher watcher, AsyncCallback.DataCallback cb, Object ctx) {
		delegate.getData(path, watcher, cb, ctx);
	}

	@Override
	public Stat setData(String path, byte[] data, int version) throws KeeperException, InterruptedException {
		return delegate.setData(path, data, version);
	}

	@Override
	public void setData(String path, byte[] data, int version, AsyncCallback.StatCallback cb, Object ctx) {
		delegate.setData(path, data, version, cb, ctx);
	}

	@Override
	public List<String> getChildren(String path) throws KeeperException, InterruptedException {
		return delegate.getChildren(path);
	}

	@Override
	public List<String> getChildren(String path, Watcher watcher) throws KeeperException, InterruptedException {
		return delegate.getChildren(path, watcher);
	}

	@Override
	public void getChildren(String path, Watcher watcher, AsyncCallback.ChildrenCallback cb, Object ctx) {
		delegate.getChildren(path, watcher, cb, ctx);
	}

	@Override
	public void getChildren(String path, Watcher watcher, AsyncCallback.Children2Callback cb, Object ctx) {
		delegate.getChildren(path, watcher, cb, ctx);
	}

	@Override
	public void sync(String path, AsyncCallback.VoidCallback cb, Object ctx) {
		delegate.sync(path, cb, ctx);
	}

	@Override
	public Transaction transaction() {
		return delegate.transaction();
	}

	@Override
	public List<OpResult> multi(Iterable<Op> ops) throws InterruptedException, KeeperException {
		return delegate.multi(ops);
	}
}
