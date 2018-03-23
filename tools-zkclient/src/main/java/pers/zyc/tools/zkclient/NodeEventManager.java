package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventBus;
import pers.zyc.tools.event.MulticastExceptionHandler;
import pers.zyc.tools.lifecycle.Lifecycle;
import pers.zyc.tools.zkclient.listener.ConnectionListenerAdapter;

/**
 * @author zhangyancheng
 */
class NodeEventManager extends ConnectionListenerAdapter implements Lifecycle, Watcher {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeEventManager.class);
	static final MulticastExceptionHandler EXCEPTION_HANDLER = new LogMulticastExceptionHandler(LOGGER);
	static final WatchedEvent CHECK_WATCH_EVENT = new WatchedEvent(null, null, null);

	private final ZKClient zkClient;
	private final NodeEventTransfer nodeEventTransfer;
	private final EventBus<WatchedEvent> watchedEventBus;

	NodeEventManager(String path, ZKClient zkClient) {
		this.zkClient = zkClient;

		nodeEventTransfer = new NodeEventTransfer(path, zkClient, this);

		watchedEventBus = new EventBus<WatchedEvent>().name("NodeEventManager - " + path)
				.multicastExceptionHandler(EXCEPTION_HANDLER).addListeners(nodeEventTransfer);
	}

	@Override
	public void start() {
		zkClient.addListener(this);
		if (zkClient.isConnected()) {
			watchedEventBus.offer(CHECK_WATCH_EVENT);
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
	public void process(WatchedEvent event) {
		if (event.getPath() != null) {
			watchedEventBus.offer(event);
		}
	}

	@Override
	public void onConnected(boolean newSession) {
		watchedEventBus.offer(CHECK_WATCH_EVENT);
	}

	NodeEventTransfer getNodeEventTransfer() {
		return nodeEventTransfer;
	}
}
