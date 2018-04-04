package pers.zyc.tools.zkclient;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import pers.zyc.tools.lifecycle.Service;
import pers.zyc.tools.zkclient.listener.*;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;
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

	/**
	 * 读锁, 所有
	 */
	Lock readLock;

	/**
	 * 代理IZookeeper操作
	 */
	private final IZookeeper delegate;

	/**
	 * 连接器, 连接状态维护以及发布状态变更事件
	 */
	private final ZKConnector connector;

	/**
	 * 临时节点重建器
	 */
	private final EphemeralNodeReCreator ephemeralNodeReCreator;

	/**
	 * 节点事件发生器集合
	 */
	private final ConcurrentMap<String, NodeEventReactor> nodeEventManagers;

	/**
	 * 客户端配置
	 */
	private final ClientConfig config;

	public ZKClient(ClientConfig config) {
		this.config = config;

		nodeEventManagers = new ConcurrentHashMap<>();

		connector = new ZKConnector(config.getConnectStr(), config.getSessionTimeout());

		delegate = (IZookeeper) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {IZookeeper.class},
				config.isUseRetry() ? new RetryAbleZookeeper(this) : new DefaultZookeeper(this));

		ephemeralNodeReCreator = new EphemeralNodeReCreator(this);
	}

	@Override
	protected Lock initServiceLock() {
		ReadWriteLock rwLock = new ReentrantReadWriteLock();
		this.readLock = rwLock.readLock();
		return rwLock.writeLock();
	}

	@Override
	protected void doStart() {
		connector.start();

		if (config.isSyncStart()) {
			SyncStartListener syncStartListener = new SyncStartListener();
			connector.addListener(syncStartListener);
			try {
				if (!connector.isConnected()) {
					//阻塞直到连接到成功
					syncStartListener.latch.await();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new ClientException("Interrupted", e);
			} finally {
				connector.removeListener(syncStartListener);
			}
		}
	}

	@Override
	protected void doStop() throws Exception {
		connector.stop();
		nodeEventManagers.clear();
	}

	/**
	 * @return 配置信息
	 */
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

	/**
	 * @return ZooKeeper实例
	 */
	public ZooKeeper getZooKeeper() {
		return connector.getZooKeeper();
	}

	/**
	 * @return 当前是否连通到ZooKeeper Server
	 */
	public boolean isConnected() {
		return connector.isConnected();
	}

	/**
	 * 添加连接监听器, 连接状态变更后触发回调
	 *
	 * @param connectionListener 连接监听器
	 */
	public void addListener(ConnectionListener connectionListener) {
		connector.addListener(connectionListener);
	}

	/**
	 * 移除连接监听器, 不再触发连接事件回调
	 *
	 * @param connectionListener 连接监听器
	 */
	public void removeListener(ConnectionListener connectionListener) {
		connector.removeListener(connectionListener);
	}

	/**
	 * 获取节点事件reactor, 如果首次获取则创建并启动reactor
	 *
	 * @param path 节点路径
	 * @return 节点事件reactor
	 */
	private NodeEventReactor getNodeEventReactor(String path) {
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

	/**
	 * 添加存在监听器, 节点存在状态变更后触发回调
	 *
	 * @param path 节点路径
	 * @param existsEventListener 存在监听器
	 */
	public void addListener(String path, ExistsEventListener existsEventListener) {
		getNodeEventReactor(path).existsEventReactor.addListener(existsEventListener);
	}

	/**
	 * 添加节点数据监听器, 节点数据变更后触发回调
	 *
	 * @param path 节点路径
	 * @param dataEventListener 数据监听器
	 */
	public void addListener(String path, DataEventListener dataEventListener) {
		getNodeEventReactor(path).dataEventReactor.addListener(dataEventListener);
	}

	/**
	 * 添加子节点监听器, 子节点变更后触发回调
	 *
	 * @param path 节点路径
	 * @param childrenEventListener 子节点监听器
	 */
	public void addListener(String path, ChildrenEventListener childrenEventListener) {
		getNodeEventReactor(path).childrenEventReactor.addListener(childrenEventListener);
	}

	/**
	 * 移除监听器, 不再触发事件回调
	 *
	 * @param path 节点路径
	 * @param existsEventListener 存在监听器
	 */
	public void removeListener(String path, ExistsEventListener existsEventListener) {
		NodeEventReactor nodeEventReactor = nodeEventManagers.get(path);
		if (nodeEventReactor != null) {
			nodeEventReactor.existsEventReactor.removeListener(existsEventListener);
		}
	}

	/**
	 * 移除监听器, 不再触发事件回调
	 *
	 * @param path 节点路径
	 * @param dataEventListener 数据监听器
	 */
	public void removeListener(String path, DataEventListener dataEventListener) {
		NodeEventReactor nodeEventReactor = nodeEventManagers.get(path);
		if (nodeEventReactor != null) {
			nodeEventReactor.dataEventReactor.removeListener(dataEventListener);
		}
	}

	/**
	 * 移除监听器, 不再触发事件回调
	 *
	 * @param path 节点路径
	 * @param childrenEventListener 子节点监听器
	 */
	public void removeListener(String path, ChildrenEventListener childrenEventListener) {
		NodeEventReactor nodeEventReactor = nodeEventManagers.get(path);
		if (nodeEventReactor != null) {
			nodeEventReactor.childrenEventReactor.removeListener(childrenEventListener);
		}
	}

	@Override
	public String createPersistent(String path, byte[] data, boolean sequential) throws
			KeeperException, InterruptedException, ClientException {

		return delegate.createPersistent(path, Objects.requireNonNull(data), sequential);
	}

	@Override
	public String createEphemeral(String path, byte[] data, boolean sequential) throws
			KeeperException, InterruptedException, ClientException {

		return delegate.createEphemeral(path, Objects.requireNonNull(data), sequential);
	}

	/**
	 * 创建临时节点, 重建会话时重建此临时节点(节点被手动删除时不会重建)
	 *
	 * @param path 节点路径
	 * @param data 节点数据
	 * @param sequential 是否为顺序节点
	 * @param recreateListener 重建监听器, 如果为null则不发布重建事件
	 * @return actual path
	 * @throws KeeperException ZooKeeper异常
	 * @throws InterruptedException 线程中断
	 */
	public String createEphemeral(String path, byte[] data, boolean sequential,
								  RecreateListener recreateListener)
			throws KeeperException, InterruptedException, ClientException {

		String retPath = delegate.createEphemeral(path, Objects.requireNonNull(data), sequential);
		ephemeralNodeReCreator.add(path, data, sequential, recreateListener);
		return retPath;
	}

	/**
	 * 更新重建节点的数据(不会立即更新zookeeper节点上的值, 重建节点时更新)
	 *
	 * @param path 节点路径
	 * @param data 节点数据
	 */
	public void updateEphemeralData(String path, byte[] data) {
		ephemeralNodeReCreator.updateData(path, Objects.requireNonNull(data));
	}

	/**
	 * 移除临时节点重建(此方法不会删除临时节点, 只是从重建列表中删除, 后续新会话时不再重建此节点)
	 *
	 * @param path 临时节点路径
	 */
	public void removeEphemeral(String path) {
		ephemeralNodeReCreator.remove(path);
	}

	@Override
	public void delete(String path) throws KeeperException, InterruptedException, ClientException {
		delegate.delete(path);
	}

	@Override
	public void delete(String path, int version) throws KeeperException, InterruptedException, ClientException {
		delegate.delete(path, version);
	}

	@Override
	public void delete(String path, int version, AsyncCallback.VoidCallback cb, Object ctx) throws ClientException {
		delegate.delete(path, version, cb, ctx);
	}

	@Override
	public boolean exists(String path) throws KeeperException, InterruptedException, ClientException {
		return delegate.exists(path);
	}

	@Override
	public Stat exists(String path, Watcher watcher) throws KeeperException, InterruptedException, ClientException {
		return delegate.exists(path, watcher);
	}

	@Override
	public void exists(String path, Watcher watcher, AsyncCallback.StatCallback cb, Object ctx) throws ClientException {
		delegate.exists(path, watcher, cb, ctx);
	}

	@Override
	public byte[] getData(String path) throws KeeperException, InterruptedException, ClientException {
		return delegate.getData(path);
	}

	@Override
	public byte[] getData(String path, Watcher watcher) throws KeeperException, InterruptedException, ClientException {
		return delegate.getData(path, watcher);
	}

	@Override
	public byte[] getData(String path, Watcher watcher, Stat stat) throws
			KeeperException, InterruptedException, ClientException {

		return delegate.getData(path, watcher, stat);
	}

	@Override
	public void getData(String path, Watcher watcher, AsyncCallback.DataCallback cb, Object ctx)
			throws ClientException {

		delegate.getData(path, watcher, cb, ctx);
	}

	@Override
	public Stat setData(String path, byte[] data, int version) throws
			KeeperException, InterruptedException, ClientException {

		return delegate.setData(path, Objects.requireNonNull(data), version);
	}

	@Override
	public void setData(String path, byte[] data, int version, AsyncCallback.StatCallback cb, Object ctx)
			throws ClientException {

		delegate.setData(path, Objects.requireNonNull(data), version, cb, ctx);
	}

	@Override
	public List<String> getChildren(String path) throws KeeperException, InterruptedException, ClientException {
		return delegate.getChildren(path);
	}

	@Override
	public List<String> getChildren(String path, Watcher watcher) throws
			KeeperException, InterruptedException, ClientException {

		return delegate.getChildren(path, watcher);
	}

	@Override
	public void getChildren(String path, Watcher watcher, AsyncCallback.ChildrenCallback cb, Object ctx)
			throws ClientException {

		delegate.getChildren(path, watcher, cb, ctx);
	}

	@Override
	public void getChildren(String path, Watcher watcher, AsyncCallback.Children2Callback cb, Object ctx)
			throws ClientException {

		delegate.getChildren(path, watcher, cb, ctx);
	}

	@Override
	public void sync(String path, AsyncCallback.VoidCallback cb, Object ctx) throws ClientException {
		delegate.sync(path, cb, ctx);
	}

	@Override
	public Transaction transaction() throws ClientException {
		return delegate.transaction();
	}

	@Override
	public List<OpResult> multi(Iterable<Op> ops) throws InterruptedException, KeeperException, ClientException {
		return delegate.multi(ops);
	}
}
