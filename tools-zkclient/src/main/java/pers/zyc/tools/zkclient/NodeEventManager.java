package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventBus;
import pers.zyc.tools.event.MulticastExceptionHandler;
import pers.zyc.tools.lifecycle.Service;
import pers.zyc.tools.zkclient.listener.ConnectionListenerAdapter;

/**
 * @author zhangyancheng
 */
class NodeEventManager extends Service {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeEventManager.class);
	static final MulticastExceptionHandler EXCEPTION_HANDLER = new LogMulticastExceptionHandler(LOGGER);
	static final WatchedEvent START_EVENT = new WatchedEvent(null, null, null);

	private final ZKClient zkClient;
	private final ConnectionListener connectionListener = new ConnectionListener();

	private final NodeEventTransfer nodeEventTransfer;
	private final EventBus<WatchedEvent> watchedEventBus;

	NodeEventManager(String path, ZKClient zkClient) {
		this.zkClient = zkClient;

		nodeEventTransfer = new NodeEventTransfer(path, zkClient, new NodeEventWatcher());

		watchedEventBus = new EventBus<WatchedEvent>().name("NodeEventManager - " + path)
				.multicastExceptionHandler(EXCEPTION_HANDLER).addListeners(nodeEventTransfer);
	}

	@Override
	protected void doStart() {
		watchedEventBus.start();
		zkClient.addListener(connectionListener);
		if (zkClient.isConnected()) {
			watchedEventBus.offer(START_EVENT);
		}
	}

	@Override
	protected void doStop() throws Exception {
		watchedEventBus.stop();
	}

	private class NodeEventWatcher implements Watcher {

		@Override
		public void process(WatchedEvent event) {
			if (event.getPath() != null) {
				watchedEventBus.offer(event);
			}
		}
	}

	private class ConnectionListener extends ConnectionListenerAdapter {

		@Override
		public void onConnected(boolean newSession) {
			watchedEventBus.offer(START_EVENT);
		}

		@Override
		public void onDisconnected(boolean sessionClosed) {
			if (sessionClosed) {
				nodeEventTransfer.allResetWatch();
			}
		}
	}

	NodeEventTransfer getNodeEventTransfer() {
		return nodeEventTransfer;
	}
}
