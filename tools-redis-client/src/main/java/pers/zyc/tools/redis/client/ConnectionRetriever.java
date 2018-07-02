package pers.zyc.tools.redis.client;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.lifecycle.PeriodicService;

import java.util.concurrent.*;

/**
 * @author zhangyancheng
 */
class ConnectionRetriever extends PeriodicService implements EventListener<ConnectionEvent> {
	private final static Logger LOGGER = LoggerFactory.getLogger(ConnectionRetriever.class);

	private final GenericObjectPool<Connection> pool;
	private final BlockingQueue<ConnectionEvent> retrieverEventQueue = new LinkedBlockingDeque<>();

	ConnectionRetriever(GenericObjectPool<Connection> pool) {
		this.pool = pool;
	}

	@Override
	protected long period() {
		return 0;
	}

	@Override
	protected void execute() throws InterruptedException {
		ConnectionEvent event = retrieverEventQueue.take();
		try {
			switch (event.eventType) {
				case REQUEST_TIMEOUT:
				case EXCEPTION_CAUGHT:
					pool.invalidateObject(event.getSource());
					break;
				case RESPONSE_RECEIVED:
					pool.returnObject(event.getSource());
					break;
			}
		} catch (Exception e) {
			LOGGER.error("Connection recycle error", e);
		}
	}

	@Override
	public void onEvent(final ConnectionEvent event) {
		LOGGER.debug("Retriever: {}", event);

		switch (event.eventType) {
			case REQUEST_SET:
			case REQUEST_SEND:
			case CONNECTION_CLOSED:
				return;
		}

		final Connection connection = event.getSource();
		connection.removeListener(this);

		retrieverEventQueue.offer(event);
	}
}
