package pers.zyc.tools.redis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.lifecycle.PeriodicService;
import pers.zyc.tools.utils.TimeMillis;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhangyancheng
 */
class NetWorker extends PeriodicService implements EventListener<ConnectionEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetWorker.class);

	private final long requestTimeout;
	private final Selector selector = Selector.open();
	private final AtomicBoolean wakeUp = new AtomicBoolean();
	private final Map<Connection, Long> requestingConnectionMap = new LinkedHashMap<>();

	NetWorker(long requestTimeout) throws IOException {
		this.requestTimeout = requestTimeout;
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();
		selector.close();
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		LOGGER.error("Uncaught exception", e);
		super.uncaughtException(t, e);
	}

	@Override
	public void onEvent(ConnectionEvent event) {
		Connection connection = event.getSource();
		switch (event.eventType) {
			case REQUEST_SET:
				updateInterestOps(keyFor(connection), SelectionKey.OP_WRITE);
				wakeUp();
				break;
			case REQUEST_SEND:
				updateInterestOps(keyFor(connection), SelectionKey.OP_READ);
				requestingConnectionMap.put(connection, TimeMillis.get() + requestTimeout);
				break;
			case RESPONSE_RECEIVED:
				requestingConnectionMap.remove(connection);
				break;
			case CONNECTION_CLOSED:
				keyFor(connection).cancel();
				break;
			default:
				LOGGER.debug("OnEvent: {}", event);
		}
	}

	private SelectionKey keyFor(Connection connection) {
		return connection.channel.keyFor(selector);
	}

	private void checkTimeoutConnection() {
		Iterator<Map.Entry<Connection, Long>> iterator = requestingConnectionMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Connection, Long> entry = iterator.next();
			if (entry.getValue() < TimeMillis.get()) {
				entry.getKey().timeout();
				iterator.remove();
			}
		}
	}

	void register(Connection connection) throws IOException {
		connection.addListener(this);
		synchronized (this) {
			wakeUp();
			connection.channel.register(selector, SelectionKey.OP_READ, connection);
		}
	}

	private static void updateInterestOps(SelectionKey sk, int interestOps) {
		sk.interestOps(interestOps);
	}

	private void wakeUp() {
		if (wakeUp.compareAndSet(false, true)) {
			selector.wakeup();
		}
	}

	@Override
	protected long period() {
		return 0;
	}

	private void doSelect() {
		try {
			selector.select(1000);

			if (wakeUp.compareAndSet(true, false)) {
				selector.selectNow();
			}
		} catch (IOException e) {
			throw new RedisClientException(e);
		}
	}

	@Override
	protected void execute() throws InterruptedException {
		doSelect();

		if (!isRunning()) {
			return;
		}

		Set<SelectionKey> selected;
		synchronized (this) {
			selected = selector.selectedKeys();
		}

		if (selected.isEmpty()) {
			checkTimeoutConnection();
			return;
		}

		Iterator<SelectionKey> i = selected.iterator();

		while (i.hasNext()) {
			SelectionKey sk = i.next();
			i.remove();

			if (!sk.isValid()) {
				continue;
			}

			Connection connection = (Connection) sk.attachment();
			if (sk.isWritable()) {
				connection.write();
			}
			if (sk.isReadable()) {
				connection.read();
			}
		}
	}
}
