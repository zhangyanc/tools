package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventBus;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.event.MulticastExceptionHandler;
import pers.zyc.tools.event.Multicaster;
import pers.zyc.tools.lifecycle.Service;
import pers.zyc.tools.zkclient.listener.ConnectionListenerAdapter;
import pers.zyc.tools.zkclient.listener.ExistsEventListener;
import pers.zyc.tools.zkclient.listener.NodeDataEventListener;

/**
 * @author zhangyancheng
 */
public class NodeEventManager extends Service {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeEventManager.class);
	private static MulticastExceptionHandler EXCEPTION_HANDLER = new LogMulticastExceptionHandler(LOGGER);

	/**
	 * 节点路径
	 */
	private final String path;

	private final NodeWatcher nodeWatcher = new NodeWatcher();
	private final EventBus<WatchedEvent> watchedEventBus = new EventBus<>();
	private final Multicaster<ExistsEventListener> existsEventMulticaster = new Multicaster<ExistsEventListener>() {};
	private final Multicaster<NodeDataEventListener> nodeDataEventMulticaster = new Multicaster<NodeDataEventListener>() {};

	public NodeEventManager(String path) {
		this.path = path;
	}

	@Override
	protected void doStart() {
		watchedEventBus.name("NodeEventManager - " + path);
		watchedEventBus.multicastExceptionHandler(EXCEPTION_HANDLER);
		watchedEventBus.addListener(new WatchedEventListener());
		watchedEventBus.start();

		existsEventMulticaster.setExceptionHandler(EXCEPTION_HANDLER);
		existsEventMulticaster.setExceptionHandler(EXCEPTION_HANDLER);
	}

	@Override
	protected void doStop() throws Exception {

	}

	private class ConnectionListener extends ConnectionListenerAdapter {
		@Override
		public void onConnected() {

		}
	}

	private class WatchedEventListener implements EventListener<WatchedEvent> {

		@Override
		public void onEvent(WatchedEvent event) {

		}
	}

	private class NodeWatcher implements Watcher {

		@Override
		public void process(WatchedEvent event) {
			watchedEventBus.offer(event);
		}
	}

	public void addListener(ExistsEventListener existsEventListener) {
		existsEventMulticaster.addListener(existsEventListener);
	}

	public void removeListener(ExistsEventListener existsEventListener) {
		existsEventMulticaster.removeListener(existsEventListener);
	}

	public void addListener(NodeDataEventListener nodeDataEventListener) {
		nodeDataEventMulticaster.addListener(nodeDataEventListener);
	}

	public void removeListener(NodeDataEventListener nodeDataEventListener) {
		nodeDataEventMulticaster.removeListener(nodeDataEventListener);
	}
}
