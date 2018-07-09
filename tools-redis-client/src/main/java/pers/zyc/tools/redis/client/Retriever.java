package pers.zyc.tools.redis.client;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.lifecycle.PeriodicService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author zhangyancheng
 */
class Retriever extends PeriodicService implements EventListener<ConnectionEvent> {
	private final static Logger LOGGER = LoggerFactory.getLogger(Retriever.class);

	private final GenericObjectPool<Connection> pool;
	private final BlockingQueue<ConnectionEvent> retrieverEventQueue = new LinkedBlockingDeque<>();

	Retriever(GenericObjectPool<Connection> pool) {
		this.pool = pool;
	}

	@Override
	protected void doStop() throws Exception {
		List<ConnectionEvent> remain = new ArrayList<>();
		if (retrieverEventQueue.drainTo(remain) == 0) {
			return;
		}
		for (ConnectionEvent event : remain) {
			retrieve(event);
		}
	}

	@Override
	protected long getInterval() {
		return 0;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		LOGGER.error("Uncaught exception, ConnectionRetriever stopped.", e);
		super.uncaughtException(t, e);
	}

	@Override
	protected void execute() throws InterruptedException {
		retrieve(retrieverEventQueue.take());
	}

	private void retrieve(ConnectionEvent event) {
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
