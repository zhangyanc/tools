package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.event.Multicaster;
import pers.zyc.tools.zkclient.listener.ChildrenListener;
import pers.zyc.tools.zkclient.listener.ExistsEventListener;
import pers.zyc.tools.zkclient.listener.NodeDataEventListener;
import pers.zyc.tools.zkclient.listener.NodeListener;

import java.util.Arrays;
import java.util.List;

import static org.apache.zookeeper.Watcher.Event.EventType;

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

	NodeEventTransfer(String path, ZKClient zkClient, Watcher watcher) {
		this.path = path;
		this.zkClient = zkClient;
		this.watcher = watcher;
	}

	@Override
	public void onEvent(WatchedEvent event) {
		if (event == NodeEventManager.START_EVENT) {
			allWatch();
			return;
		}

		EventType eventType = event.getType();
		switch (eventType) {
			case NodeCreated:
			case NodeDeleted:
				existsTransfer.watch(true);
				break;
			case NodeDataChanged:
				break;
			case NodeChildrenChanged:
				break;
			default:
				throw new RuntimeException("Unexpected type: " + eventType);
		}
	}

	private void allWatch() {
		if (!existsTransfer.watched) {
			existsTransfer.watch(false);
		}
		if (!dataTransfer.watched) {
			dataTransfer.watch(false);
		}
		if (!childrenTransfer.watched) {
			childrenTransfer.watch(false);
		}
	}

	void allResetWatch() {
		existsTransfer.watched = false;
		dataTransfer.watched = false;
		childrenTransfer.watched = false;
	}

	private abstract class EventTransfer<D, L extends NodeListener> {
		D data;
		boolean watched;
		Multicaster<L> multicaster;

		EventTransfer(Multicaster<L> multicaster) {
			this.multicaster = multicaster;
			multicaster.setExceptionHandler(NodeEventManager.EXCEPTION_HANDLER);
		}

		void watch(boolean reWatch) {
			try {
				doWatch(reWatch);
				watched = true;
			} catch (Exception e) {
				watched = false;
				LOGGER.error("Add watcher error, path: " + path, e);
				if (e instanceof InterruptedException) {
					Thread.currentThread().interrupt();
				}
			}
		}

		protected abstract void doWatch(boolean fromWatcher) throws Exception;
	}

	private class ExistsTransfer extends EventTransfer<Stat, ExistsEventListener> {

		ExistsTransfer() {
			super(new Multicaster<ExistsEventListener>() {});
		}

		@Override
		protected void doWatch(boolean reWatch) throws Exception {
			Stat oldStat = data;
			data = zkClient.exists(path, watcher);
			if (!reWatch) {
				return;
			}
			//比较数据变换后发布事件
			if (data != null && oldStat == null) {
				multicaster.listeners.onNodeCreated(path, data);
			}
			if (data == null && oldStat != null) {
				multicaster.listeners.onNodeDeleted();
			}
			if (data != null && oldStat != null && !data.equals(oldStat)) {
				dataTransfer.multicaster.listeners.onStatChanged(data);
			}
		}
	}

	private class DataTransfer extends EventTransfer<byte[], NodeDataEventListener> {

		DataTransfer() {
			super(new Multicaster<NodeDataEventListener>() {});
		}

		@Override
		protected void doWatch(boolean reWatch) throws Exception {
			byte[] oldData = data;
			data = zkClient.getData(path, watcher);
			if (!reWatch) {
				return;
			}

			if (!Arrays.equals(data, oldData)) {
				System.arraycopy(data, 0, oldData, 0, data.length);
				multicaster.listeners.onDataChanged(oldData);
			}
		}
	}

	private class ChildrenTransfer extends EventTransfer<List<String>, ChildrenListener> {

		ChildrenTransfer() {
			super(new Multicaster<ChildrenListener>() {});
		}

		@Override
		protected void doWatch(boolean reWatch) throws Exception {

		}
	}

	public void addListener(ExistsEventListener existsEventListener) {
		existsTransfer.multicaster.addListener(existsEventListener);
	}

	public void removeListener(ExistsEventListener existsEventListener) {
		existsTransfer.multicaster.removeListener(existsEventListener);
	}

	public void addListener(NodeDataEventListener nodeDataEventListener) {
		dataTransfer.multicaster.addListener(nodeDataEventListener);
	}

	public void removeListener(NodeDataEventListener nodeDataEventListener) {
		dataTransfer.multicaster.removeListener(nodeDataEventListener);
	}
}
