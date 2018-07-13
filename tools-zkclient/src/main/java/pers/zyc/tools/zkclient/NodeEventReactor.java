package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.utils.Pair;
import pers.zyc.tools.utils.event.Listenable;
import pers.zyc.tools.utils.event.MulticastExceptionHandler;
import pers.zyc.tools.utils.event.Multicaster;
import pers.zyc.tools.zkclient.listener.ChildrenEventListener;
import pers.zyc.tools.zkclient.listener.DataEventListener;
import pers.zyc.tools.zkclient.listener.ExistsEventListener;
import pers.zyc.tools.zkclient.listener.NodeEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.apache.zookeeper.Watcher.Event.EventType.*;

/**
 * 节点事件反应器, 通过连续watcher实现事件持续发布
 *
 * @author zhangyancheng
 */
class NodeEventReactor extends BaseReactor {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeEventReactor.class);

	/**
	 * 异常处理器, 所有事件发布异常都将记录日志
	 */
	private static final MulticastExceptionHandler EXCEPTION_HANDLER = new LogMulticastExceptionHandler(LOGGER);

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
	 * 每次exists后的节点存在状态
	 */
	private boolean nodeExists;

	NodeEventReactor(String path, ZKClient zkClient) {
		super(path, zkClient);
	}

	@Override
	public void onConnected(boolean newSession) {
		if (newSession) {
			enqueueEvent(CONNECTED_EVENT);
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
	protected void react(WatchedEvent event) {
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
			LOGGER.warn("Node event[{}] react error: {}", event, e.getMessage());
		}
	}

	private abstract class EventReactor<D, L extends NodeEventListener> implements Listenable<L> {

		/**
		 * 共用同一个watcher可避免重复处理事件
		 */
		final Watcher watcher = NodeEventReactor.this;

		/**
		 * 监听到的数据(用于判断变更后发布事件)
		 */
		D data;

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
			D oldData = data;

			if (!nodeExists) {
				if (oldData != null) {
					publishNodeDeleted();
				}
				return;
			}

			data = reWatch();

			publishIfDataChanged(oldData);
		}

		/**
		 * 注册ZooKeeper Watcher并获取数据
		 *
		 * @return 状态数据
		 * @throws Exception ZooKeeper调用异常
		 */
		abstract D reWatch() throws Exception;

		/**
		 * 判断数据变化后发布事件
		 */
		abstract void publishIfDataChanged(D oldData);

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
			Stat oldData = data;
			data = reWatch();

			nodeExists = data != null;

			if (firstWatch) {
				firstWatch = false;
				return;
			}

			publishIfDataChanged(oldData);
		}

		@Override
		Stat reWatch() throws Exception {
			return zkClient.exists(path, watcher);
		}

		@Override
		void publishIfDataChanged(Stat oldData) {
			if (oldData == null && data != null) {
				multicaster.listeners.onNodeCreated(path, data);
			} else if (oldData != null && data == null) {
				publishNodeDeleted();
			}
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
		void publishIfDataChanged(PathData oldData) {
			if (oldData != null && !oldData.equals(data)) {
				multicaster.listeners.onDataChanged(path, data.key(), data.value());
			}
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
		void publishIfDataChanged(List<String> oldData) {
			if (oldData != null) {
				boolean changed = oldData.size() != data.size();
				if (!changed) {
					oldData.removeAll(data);
					changed = !oldData.isEmpty();
				}
				if (changed) {
					multicaster.listeners.onChildrenChanged(path, data);
				}
			}
		}
	}
}
