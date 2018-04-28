package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import pers.zyc.tools.event.EventBus;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.lifecycle.Service;
import pers.zyc.tools.zkclient.listener.ConnectionListener;

import static org.apache.zookeeper.Watcher.Event.EventType.None;

/**
 * ZooKeeper事件反应器(异步、连接状态监听)
 *
 * @author zhangyancheng
 */
abstract class BaseReactor extends Service implements ConnectionListener, Watcher {

	/**
	 * 连接成功(包括启动时已连接、自动重连成功、session切换)后的watcher注册事件
	 */
	static final WatchedEvent CONNECTED_EVENT = new WatchedEvent(null, null, null);

	/**
	 * 监听连接变更、执行zookeeper exists、getData、getChildren
	 */
	final ZKClient zkClient;

	/**
	 * 使ZooKeeper事件(所有节点watcher接受的WatchedEvent)处理异步化
	 */
	private final EventBus<WatchedEvent> watchedEventBus = new EventBus<>();

	BaseReactor(ZKClient zkClient) {
		this.zkClient = zkClient;
	}

	@Override
	protected void doStart() {
		//注册连接监听器, 重连成功后注册watcher
		zkClient.addListener(this);

		if (zkClient.isConnected()) {
			//当前已经连接注册watcher
			enqueueEvent(CONNECTED_EVENT);
		}

		watchedEventBus.name(getName()).addListeners(new WatchedEventListener()).start();
	}

	@Override
	protected void doStop() {
		zkClient.removeListener(this);
		watchedEventBus.stop();
	}

	@Override
	public void onConnected(boolean newSession) {
		//重连成功注册watcher
		enqueueEvent(CONNECTED_EVENT);
	}

	@Override
	public void onDisconnected(boolean sessionClosed) {
	}

	@Override
	public void process(WatchedEvent event) {
		if (event.getType() != None) {
			enqueueEvent(event);
		}
	}

	/**
	 * 事件入队, 待异步处理
	 *
	 * @param event 事件
	 */
	void enqueueEvent(WatchedEvent event) {
		watchedEventBus.offer(event);
	}

	private class WatchedEventListener implements EventListener<WatchedEvent> {

		@Override
		public void onEvent(WatchedEvent event) {
			react(event);
		}
	}

	/**
	 * 反应
	 *
	 * @param event ZooKeeper Watched Event
	 */
	protected abstract void react(WatchedEvent event);
}
