package pers.zyc.tools.zkclient;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.event.Listenable;
import pers.zyc.tools.event.Multicaster;
import pers.zyc.tools.utils.Pair;
import pers.zyc.tools.zkclient.listener.ChildrenListener;
import pers.zyc.tools.zkclient.listener.DataEventListener;
import pers.zyc.tools.zkclient.listener.ExistsEventListener;
import pers.zyc.tools.zkclient.listener.NodeEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author zhangyancheng
 */
class NodeEventTransfer implements EventListener<WatchedEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeEventTransfer.class);

	private final String path;
	private final ZKClient zkClient;
	private final Watcher watcher;
	private final ExistsTransfer existsTransfer = new ExistsTransfer();
	private final DataTransfer dataTransfer = new DataTransfer();
	private final ChildrenTransfer childrenTransfer = new ChildrenTransfer();
	private boolean nodeExists = false;

	NodeEventTransfer(String path, ZKClient zkClient, Watcher watcher) {
		this.path = path;
		this.zkClient = zkClient;
		this.watcher = watcher;
	}

	@Override
	public void onEvent(WatchedEvent event) {
		if (event == NodeEventManager.CHECK_WATCH_EVENT) {
			allWatch();
			return;
		}

		switch (event.getType()) {
			case NodeCreated:
			case NodeDeleted:
				allWatch();
				break;
			case NodeDataChanged:
				dataTransfer.watch();
				break;
			case NodeChildrenChanged:
				childrenTransfer.watch();
				break;
			default:
				throw new Error("Error type!");
		}
	}

	private void allWatch() {
		existsTransfer.watch();
		dataTransfer.watch();
		childrenTransfer.watch();
	}

	abstract class EventTransfer<D, L extends NodeEventListener> implements Listenable<L> {
		D data;
		Multicaster<L> multicaster;
		/**
		 * 首次注册监听, 无法判断"变更"不发布事件
		 */
		boolean firstWatch = true;

		EventTransfer(Multicaster<L> multicaster) {
			this.multicaster = multicaster;
			multicaster.setExceptionHandler(NodeEventManager.EXCEPTION_HANDLER);
		}

		void watch() {
			try {
				doWatch();
				if (firstWatch) {
					firstWatch = false;
				}
			} catch (Exception e) {
				LOGGER.error("Add watcher error, path: " + path, e);

				if (e instanceof InterruptedException) {
					Thread.currentThread().interrupt();
					return;
				}

				if (e instanceof KeeperException.NoNodeException) {
					nodeExists = false;
				}
			}
		}

		@Override
		public void addListener(L listener) {
			multicaster.addListener(listener);
		}

		@Override
		public void removeListener(L listener) {
			multicaster.removeListener(listener);
		}

		protected abstract void doWatch() throws Exception;
	}

	class ExistsTransfer extends EventTransfer<Stat, ExistsEventListener> {

		ExistsTransfer() {
			super(new Multicaster<ExistsEventListener>() {});
		}

		@Override
		protected void doWatch() throws Exception {
			Stat oldStat = data;
			data = zkClient.exists(path, watcher);
			nodeExists = data != null;

			if (firstWatch) {
				return;
			}
			if (data != null && oldStat == null) {
				multicaster.listeners.onNodeCreated(path, data);
			}
			if (data == null && oldStat != null) {
				multicaster.listeners.onNodeDeleted(path);
			}
		}
	}

	class DataTransfer extends EventTransfer<DataTransfer.PathData, DataEventListener> {

		DataTransfer() {
			super(new Multicaster<DataEventListener>() {});
		}

		@Override
		protected void doWatch() throws Exception {
			if (!nodeExists) {
				return;
			}

			PathData oldData = data;
			Stat stat = new Stat();
			byte[] pathData = zkClient.getData(path, watcher, stat);
			data = new PathData(stat, pathData);

			if (firstWatch) {
				return;
			}
			if (!data.equals(oldData)) {
				multicaster.listeners.onDataChanged(path, stat, pathData);
			}
		}


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

	}

	class ChildrenTransfer extends EventTransfer<List<String>, ChildrenListener> {

		ChildrenTransfer() {
			super(new Multicaster<ChildrenListener>() {});
		}

		@Override
		protected void doWatch() throws Exception {
			if (!nodeExists) {
				return;
			}
			List<String> oldData = data;
			data = zkClient.getChildren(path, watcher);

			if (firstWatch) {
				return;
			}
			if (!data.equals(oldData)) {
				multicaster.listeners.onChildrenChanged(path, data);
			}
		}
	}

	ExistsTransfer getExistsTransfer() {
		return existsTransfer;
	}

	DataTransfer getDataTransfer() {
		return dataTransfer;
	}

	ChildrenTransfer getChildrenTransfer() {
		return childrenTransfer;
	}
}
