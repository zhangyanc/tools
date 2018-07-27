package pers.zyc.tools.zkclient;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.utils.event.Listenable;
import pers.zyc.tools.utils.event.MulticastExceptionHandler;
import pers.zyc.tools.utils.event.Multicaster;
import pers.zyc.tools.zkclient.listener.ClientDestroyListener;
import pers.zyc.tools.zkclient.listener.ConnectionListener;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhangyancheng
 */
public class ZKClient implements IZookeeper, Listenable<ClientDestroyListener> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZKClient.class);

	/**
	 * 异常处理器, 所有事件发布异常都将记录日志
	 */
	private static final MulticastExceptionHandler EXCEPTION_HANDLER = new LogMulticastExceptionHandler(LOGGER);
	/**
	 * 代理IZookeeper操作
	 */
	private final IZookeeper delegate;

	/**
	 * 连接器, 连接状态维护以及发布状态变更事件
	 */
	private final ZKConnector connector;

	private final AtomicBoolean destroyed = new AtomicBoolean();

	private final Multicaster<ClientDestroyListener> multicaster = new Multicaster<ClientDestroyListener>() {
		{
			setExceptionHandler(EXCEPTION_HANDLER);
		}
	};

	/**
	 * 客户端配置
	 */
	private final ClientConfig config;

	public ZKClient(ClientConfig config) {
		this.config = config;

		connector = new ZKConnector(config.getConnectStr(), config.getSessionTimeout());

		delegate = (IZookeeper) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {IZookeeper.class},
				config.isUseRetry() ? new RetryAbleZookeeper(connector, config) : new DefaultZookeeper(connector));

		connector.start();
	}

	@Override
	public void addListener(ClientDestroyListener listener) {
		multicaster.addListener(listener);
	}

	@Override
	public void removeListener(ClientDestroyListener listener) {
		multicaster.removeListener(listener);
	}

	public void destroy() {
		if (destroyed.compareAndSet(false, true)) {
			multicaster.listeners.onDestroy();
			multicaster.removeAllListeners();
			connector.stop();
		}
	}

	/**
	 * @return 配置信息
	 */
	public ClientConfig getConfig() {
		return config;
	}

	/**
	 * 等待连接
	 *
	 * @param timeout 超时
	 * @return zookeeper是否连通
	 * @throws InterruptedException 线程被中断
	 */
	public boolean waitToConnected(long timeout) throws InterruptedException {
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
			return isConnected() || connectedLatch.await(timeout, TimeUnit.MILLISECONDS);
		} finally {
			removeConnectionListener(waitListener);
		}
	}

	/**
	 * @return ZooKeeper实例
	 */
	public ZooKeeper getZooKeeper() {
		return connector.zooKeeper;
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
	 * 获取节点事件reactor, 如果首次获取则创建并启动reactor
	 *
	 * @param nodePath 节点路径
	 * @return 节点事件reactor
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
