package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.*;
import pers.zyc.tools.lifecycle.Lifecycle;
import pers.zyc.tools.utils.Pair;
import pers.zyc.tools.zkclient.listener.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.apache.zookeeper.Watcher.Event.EventType.*;

/**
 * 节点事件反应器, 通过连续watcher实现事件持续监听
 *
 * @author zhangyancheng
 */
class NodeEventReactor extends ConnectionListenerAdapter implements Lifecycle, Watcher, EventListener<WatchedEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeEventReactor.class);

	/**
	 * 异常处理器, 所有事件发布异常都将记录日志
	 */
	private static final MulticastExceptionHandler EXCEPTION_HANDLER = new LogMulticastExceptionHandler(LOGGER);

	/**
	 * 连接成功(包括启动时已连接、自动重连成功、session切换)后的watcher注册事件
	 */
	private static final WatchedEvent CONNECTED_EVENT = new WatchedEvent(null, null, null);

	/**
	 * 节点路径
	 */
	private final String path;

	/**
	 * 监听连接变更、执行zookeeper exists、getData、getChildren
	 */
	private final ZKClient zkClient;

	/**
	 * 节点存在状态reactor, 状态变更(节点新增、删除)后发布事件
	 */
	final ExistsEventReactor existsEventReactor = new ExistsEventReactor();

	/**
	 * 节点数据reactor, 数据变更后发布事件
	 */
	final DataEventReactor dataEventReactor = new DataEventReactor();

	/**
	 * 节点子节点reactor, 子节点变更后发布事件
	 */
	final ChildrenEventReactor childrenEventReactor = new ChildrenEventReactor();

	/**
	 * 使ZooKeeper事件(所有节点watcher接受的WatchedEvent)处理异步化
	 */
	private final EventBus<WatchedEvent> watchedEventBus;

	/**
	 * 每次exists后的节点存在状态
	 */
	private boolean nodeExists;

	NodeEventReactor(String path, ZKClient zkClient) {
		this.path = path;
		this.zkClient = zkClient;

		watchedEventBus = new EventBus<WatchedEvent>().name("NodeEventReactor-" + path)
				.multicastExceptionHandler(EXCEPTION_HANDLER).addListeners(this);
	}

	@Override
	public void start() {
		//注册连接监听器, 重连成功后注册watcher
		zkClient.addListener(this);

		if (zkClient.isConnected()) {
			//当前已经连接注册watcher
			watchedEventBus.offer(CONNECTED_EVENT);
		}
		watchedEventBus.start();
	}

	@Override
	public void stop() {
		watchedEventBus.stop();
	}

	@Override
	public boolean isRunning() {
		return watchedEventBus.isRunning();
	}

	@Override
	public void onConnected(boolean newSession) {
		//重连成功注册watcher
		watchedEventBus.offer(CONNECTED_EVENT);
	}

	@Override
	public void process(WatchedEvent event) {
		if (event.getType() != None) {
			LOGGER.debug("{}", event);
			//WatchedEvent入队, 待异步处理
			watchedEventBus.offer(event);
		}
	}

	/**
	 * WatchedEvent处理及事件发布:
	 *
	 * <p>
	 *     1.当为连接成功事件、节点创建和删除事件时, 节点上没有watcher, 因此
	 * 	 	 无法确定节点、数据、子节点的"当前"状态, 所以需要exists-react确认存在状态,
	 * 	 	 如果存在则再进行data、children-react, 否则监听NodeCreated
	 * <p>
	 *     2.节点数据变更, 此时节点上仍有children watcher, 只需react数据变更
	 *       如果react失败, 则表明已经发生了NodeDeleted(在队列中等待处理), 则忽略此次节点数据变更
	 * <p>
	 *     3.子节点变更, 此时节点上仍有data watcher, 只需react子节点变更
	 *       如果react失败, 则表明已经发生了NodeDeleted(在队列中等待处理), 则忽略此次子节点变更
	 * <p>
	 *     以上情况处理完后, 保证watcher按照当前状态被正确添加
	 *     如果节点被创建一定收到NodeCreated
	 *     如果节点被删除一定收到NodeDeleted
	 *     如果节点数据及子节点变更则一定可以发布最终的变更
	 *
	 *     如果连接异常react失败, 则连接恢复后仍可根据最新状态发布变更(如果在断连期间有变更)
	 *
	 * @param event 监听到的节点状态变更事件或者CONNECTED_EVENT
	 */
	@Override
	public void onEvent(WatchedEvent event) {
		try {
			if (event == CONNECTED_EVENT ||
				event.getType() == NodeCreated ||
				event.getType() == NodeDeleted) {

				existsEventReactor.react();
				dataEventReactor.react();
				childrenEventReactor.react();
			} else if (event.getType() == NodeDataChanged) {
				dataEventReactor.react();
			} else if (event.getType() == NodeChildrenChanged) {
				childrenEventReactor.react();
			}
		} catch (Exception e) {
			LOGGER.error("Process " + event + " failed!", e);
		}
	}

	private abstract class EventReactor<S, L extends NodeEventListener> implements Listenable<L> {

		/**
		 * 共用同一个watcher可避免重复处理事件
		 */
		final Watcher watcher = NodeEventReactor.this;

		/**
		 * 监听到的状态数据(用于判断变更后发布事件)
		 */
		S state;

		/**
		 * 事件发布器
		 */
		final Multicaster<L> multicaster;

		EventReactor(Multicaster<L> multicaster) {
			this.multicaster = multicaster;
			//listener回调异常处理器, 所有回调异常都将被记录日志
			multicaster.setExceptionHandler(EXCEPTION_HANDLER);
		}

		@Override
		public void addListener(L listener) {
			multicaster.addListener(listener);
		}

		@Override
		public void removeListener(L listener) {
			multicaster.removeListener(listener);
		}

		/**
		 * zk watch(exists、getData、getChildren)以及事件发布逻辑
		 *
		 * @throws Exception ZooKeeper调用异常
		 */
		void react() throws Exception {
			S oldState = state;

			if (!nodeExists) {
				if (oldState != null) {
					publishNodeDeleted();
				}
				return;
			}

			state = reWatch();

			if (oldState != null && !oldState.equals(state)) {
				publishStateChanged();
			}
		}

		/**
		 * 注册ZooKeeper Watcher并获取数据
		 *
		 * @return 状态数据
		 * @throws Exception ZooKeeper调用异常
		 */
		abstract S reWatch() throws Exception;

		/**
		 * 发布状态变更事件
		 */
		abstract void publishStateChanged();

		/**
		 * 发布节点删除事件
		 */
		void publishNodeDeleted() {
			multicaster.listeners.onNodeDeleted(path);
		}
	}

	class ExistsEventReactor extends EventReactor<Stat, ExistsEventListener> {

		ExistsEventReactor() {
			super(new Multicaster<ExistsEventListener>() {});
		}

		/**
		 * 第一次watch无法判断"变更"无需发布事件
		 */
		boolean firstWatch = true;

		@Override
		void react() throws Exception {
			Stat oldState = state;
			state = reWatch();

			nodeExists = state != null;

			if (firstWatch) {
				firstWatch = false;
				return;
			}

			if (oldState == null && state != null) {
				publishStateChanged();
			} else if (oldState != null && state == null) {
				publishNodeDeleted();
			}
		}

		@Override
		Stat reWatch() throws Exception {
			return zkClient.exists(path, watcher);
		}

		@Override
		void publishStateChanged() {
			multicaster.listeners.onNodeCreated(path, state);
		}
	}

	class DataEventReactor extends EventReactor<DataEventReactor.PathData, DataEventListener> {

		class PathData extends Pair<Stat, byte[]> {

			PathData(Stat stat, byte[] data) {
				key(Objects.requireNonNull(stat));
				value(Objects.requireNonNull(data));
			}

			@Override
			public boolean equals(Object obj) {
				if (obj == this) {
					return true;
				}

				if (!(obj instanceof PathData)) {
					return false;
				}

				PathData pathData = (PathData) obj;
				return key().equals(pathData.key()) && Arrays.equals(value(), pathData.value());
			}
		}

		DataEventReactor() {
			super(new Multicaster<DataEventListener>() {});
		}

		@Override
		PathData reWatch() throws Exception {
			Stat stat = new Stat();
			byte[] pathData = zkClient.getData(path, watcher, stat);
			return new PathData(stat, pathData);
		}

		@Override
		void publishStateChanged() {
			multicaster.listeners.onDataChanged(path, state.key(), state.value());
		}
	}

	class ChildrenEventReactor extends EventReactor<List<String>, ChildrenEventListener> {

		ChildrenEventReactor() {
			super(new Multicaster<ChildrenEventListener>() {});
		}

		@Override
		List<String> reWatch() throws Exception {
			return zkClient.getChildren(path, watcher);
		}

		@Override
		void publishStateChanged() {
			multicaster.listeners.onChildrenChanged(path, state);
		}
	}
}
