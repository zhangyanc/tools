package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import pers.zyc.tools.utils.event.EventBus;
import pers.zyc.tools.utils.event.EventListener;
import pers.zyc.tools.utils.lifecycle.Service;
import pers.zyc.tools.zkclient.listener.ClientDestroyListener;
import pers.zyc.tools.zkclient.listener.ConnectionListener;

import static org.apache.zookeeper.Watcher.Event.EventType.None;

/**
 * ZooKeeper事件反应器(异步、连接状态监听)
 *
 * @author zhangyancheng
 */
abstract class Reactor extends Service implements ClientDestroyListener,
		ConnectionListener, Watcher, EventListener<WatchedEvent> {

	/**
	 * 连接成功(包括启动时已连接、自动重连成功、session切换)后的watcher注册事件
	 */
	static final WatchedEvent CONNECTED_EVENT = new WatchedEvent(null, null, null);

	/**
	 * 节点路径
	 */
	final String path;

	/**
	 * 监听连接变更、执行zookeeper exists、getData、getChildren
	 */
	final ZKClient zkClient;

	/**
	 * 使ZooKeeper事件(所有节点watcher接受的WatchedEvent)处理异步化
	 */
	private final EventBus<WatchedEvent> watchedEventBus;

	Reactor(String path, ZKClient zkClient) {
		this.path = path;
		this.zkClient = zkClient;
		watchedEventBus = new EventBus.Builder<WatchedEvent>().name(getName()).build();
		zkClient.addListener(this);
	}

	@Override
	protected void doStart() {
		watchedEventBus.addListener(this);
		watchedEventBus.start();

		//注册连接监听器, 重连成功后注册watcher
		zkClient.addConnectionListener(this);
		if (zkClient.isConnected()) {
			//当前已经连接注册watcher
			enqueueEvent(CONNECTED_EVENT);
		}
	}

	@Override
	protected void doStop() {
		watchedEventBus.stop();
		zkClient.removeConnectionListener(this);
	}

	@Override
	public String getName() {
		return super.getName() + "-" + path;
	}

	public String getPath() {
		return path;
	}

	@Override
	public void onDestroy() {
		stop();
	}

	@Override
	public void onConnected(boolean newSession) {
	}

	@Override
	public void onDisconnected(boolean sessionClosed) {
	}

	/**
	 * 事件入队, 待异步处理
	 *
	 * @param event 事件
	 */
	void enqueueEvent(WatchedEvent event) {
		watchedEventBus.offer(event);
	}

	@Override
	public void process(WatchedEvent event) {
		if (event.getType() != None) {
			enqueueEvent(event);
		}
	}

	@Override
	public void onEvent(WatchedEvent event) {
		react(event);
	}

	/**
	 * 反应
	 *
	 * @param event ZooKeeper Watched Event
	 */
	protected abstract void react(WatchedEvent event);
}
