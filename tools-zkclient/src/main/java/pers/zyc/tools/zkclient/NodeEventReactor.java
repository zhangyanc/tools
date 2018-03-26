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
 * @author zhangyancheng
 */
class NodeEventReactor extends ConnectionListenerAdapter implements Lifecycle, EventListener<WatchedEvent> {
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
	 * 节点存在状态re-watcher, 状态变更(节点新增、删除)后发布事件
	 */
	final ExistsReWatcher existsReWatcher = new ExistsReWatcher();

	/**
	 * 节点data re-watcher, 数据变更后发布事件
	 */
	final DataReWatcher dataReWatcher = new DataReWatcher();

	/**
	 * 节点子节点re-watcher, 子节点变更后发布事件
	 */
	final ChildrenReWatcher childrenReWatcher = new ChildrenReWatcher();

	/**
	 * 使ZooKeeper事件(所有节点watcher接受的WatchedEvent)处理异步化
	 */
	private EventBus<WatchedEvent> watchedEventBus;

	NodeEventReactor(String path, ZKClient zkClient) {
		this.path = path;
		this.zkClient = zkClient;
	}

	@Override
	public void start() {
		//注册连接监听器, 重连成功后注册watcher
		zkClient.addListener(this);

		//设置异步处理线程名、异常处理器、WatchedEvent 处理回调
		watchedEventBus = new EventBus<WatchedEvent>().name("NodeEventReactor" + path.replace('/', '-'))
				.multicastExceptionHandler(EXCEPTION_HANDLER).addListeners(this);

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

	/**
	 * WatchedEvent异步处理:
	 * <p>
	 *     1.当为连接成功事件、节点创建和删除事件时, 当前节点是没有watcher的, 没有watcher则
	 * 	 	 无法确定节点、数据、子节点的任何状态, 所以首先需要exists检查存在状态并注册exists watcher.
	 * 	 	 如果检查节点存在再注册数据及子节点watcher
	 * <p>
	 *     2.节点数据变更, 此时节点上仍有children watcher(可以收到NodeDeleted), 只需re-watch数据
	 *       如果re-watch失败, 则表明已经发生了NodeDeleted(在队列中等待处理), 则忽略此次变更
	 * <p>
	 *     3.子节点变更, 此时节点上仍有data watcher(可以收到NodeDeleted), 只需re-watch子节点
	 *       如果re-watch失败, 则表明已经发生了NodeDeleted(在队列中等待处理), 则忽略此次变更
	 * <p>
	 *     以上情况处理完后, 保证watcher按照当前状态被正确的添加了.
	 *     如果节点被创建一定收到NodeCreated
	 *     如果节点被删除一定收到NodeDeleted
	 *     如果节点数据及子节点变更则一定可以发布最终的变更
	 *
	 *
	 * @param event 监听到的节点状态变更事件或者CONNECTED_EVENT
	 */
	@Override
	public void onEvent(WatchedEvent event) {
		if (event == CONNECTED_EVENT ||
			event.getType() == NodeCreated ||
			event.getType() == NodeDeleted) {

			if (existsReWatcher.reWatch() && existsReWatcher.nodeExists) {
				dataReWatcher.reWatch();
				childrenReWatcher.reWatch();
			}
		} else if (event.getType() == NodeDataChanged) {
			dataReWatcher.reWatch();
		} else if (event.getType() == NodeChildrenChanged) {
			childrenReWatcher.reWatch();
		} else {
			throw new Error("Invalid " + event);
		}
	}

	private abstract class ReWatcher<D, L extends NodeEventListener> implements Listenable<L>, Watcher {
		/**
		 * 监听到的数据(用于判断变更然后发布事件)
		 */
		D data;

		/**
		 * 记录是否为第一次watch, 第一次watch无法判断"变更"无需发布事件
		 */
		boolean firstWatch = true;

		/**
		 * 事件发布器
		 */
		Multicaster<L> multicaster;

		ReWatcher(Multicaster<L> multicaster) {
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

		@Override
		public void process(WatchedEvent event) {
			if (event.getPath() != null) {
				//WatchedEvent入队, 待异步处理
				watchedEventBus.offer(event);
			}
		}

		/**
		 * 收到变更事件后re-watch, 判断变更后发布事件
		 *
		 * @return 是否re-watch成功
		 */
		boolean reWatch() {
			try {
				doWatch();
				if (firstWatch) {
					firstWatch = false;
				}
				return true;
			} catch (Exception e) {
				LOGGER.error("Watch error, path" + path, e);

				if (e instanceof InterruptedException) {
					Thread.currentThread().interrupt();
				}

				return false;
			}
		}

		/**
		 * 具体的watch以及事件发布逻辑(exists、getData、getChildren)
		 *
		 * @throws Exception ZooKeeper调用异常
		 */
		protected abstract void doWatch() throws Exception;
	}

	class ExistsReWatcher extends ReWatcher<Stat, ExistsEventListener> {

		ExistsReWatcher() {
			super(new Multicaster<ExistsEventListener>() {});
		}

		/**
		 * 每次exists后的节点存在状态, 如果不存在则不添加data、children watcher
		 */
		boolean nodeExists = false;

		@Override
		protected void doWatch() throws Exception {
			Stat oldStat = data;
			data = zkClient.exists(path, this);
			nodeExists = data != null;
			if (firstWatch) {
				return;
			}
			//exists只发布"存在变更"
			if (oldStat == null && data != null) {
				multicaster.listeners.onNodeCreated(path, data);
			}
			if (oldStat != null && data == null) {
				multicaster.listeners.onNodeDeleted(path);
			}
		}
	}

	class DataReWatcher extends ReWatcher<DataReWatcher.PathData, DataEventListener> {

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

		DataReWatcher() {
			super(new Multicaster<DataEventListener>() {});
		}

		@Override
		protected void doWatch() throws Exception {
			PathData oldData = data;
			Stat stat = new Stat();
			byte[] pathData = zkClient.getData(path, this);
			data = new PathData(stat, pathData);
			if (firstWatch) {
				return;
			}
			//data只发布"数据变更"
			if (!oldData.equals(data)) {
				multicaster.listeners.onDataChanged(path, data.key(), data.value());
			}
		}
	}

	class ChildrenReWatcher extends ReWatcher<List<String>, ChildrenListener> {

		ChildrenReWatcher() {
			super(new Multicaster<ChildrenListener>() {});
		}

		@Override
		protected void doWatch() throws Exception {
			List<String> oldData = data;
			data = zkClient.getChildren(path, this);
			if (firstWatch) {
				return;
			}
			//children只发布"子节点变更"
			if (!oldData.equals(data)) {
				multicaster.listeners.onChildrenChanged(path, data);
			}
		}
	}
}
