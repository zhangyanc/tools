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

	private interface TimeoutChecker {

		void put(Connection connection);

		void remove(Connection connection);

		void check();
	}

	private static TimeoutChecker NON_CHECKER = new TimeoutChecker() {

		@Override
		public void put(Connection connection) {
		}

		@Override
		public void remove(Connection connection) {
		}

		@Override
		public void check() {
		}
	};

	private static final Logger LOGGER = LoggerFactory.getLogger(NetWorker.class);

	private final Selector selector = Selector.open();
	private final AtomicBoolean wakeUp = new AtomicBoolean();
	private final TimeoutChecker timeoutChecker;

	NetWorker(final int requestTimeout) throws IOException {
		timeoutChecker = requestTimeout <= 0 ? NON_CHECKER : new TimeoutChecker() {

			private final Map<Connection, Long> requestingConnectionMap = new LinkedHashMap<>();

			@Override
			public void put(Connection connection) {
				requestingConnectionMap.put(connection, TimeMillis.get() + requestTimeout);
			}

			@Override
			public void remove(Connection connection) {
				requestingConnectionMap.remove(connection);
			}

			@Override
			public void check() {
				Iterator<Map.Entry<Connection, Long>> iterator = requestingConnectionMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<Connection, Long> entry = iterator.next();
					if (entry.getValue() < TimeMillis.get()) {
						entry.getKey().timeout();
						iterator.remove();
					}
				}
			}
		};
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
				enableWrite(connection);
				wakeUp();
				break;
			case REQUEST_SEND:
				disableWrite(connection);
				timeoutChecker.put(connection);
				break;
			case RESPONSE_RECEIVED:
			case CONNECTION_CLOSED:
			case EXCEPTION_CAUGHT:
				timeoutChecker.remove(connection);
				break;
			default:
				LOGGER.debug("OnEvent: {}", event);
		}
	}

	private SelectionKey keyFor(Connection connection) {
		return connection.channel.keyFor(selector);
	}

	private void checkTimeoutConnection() {
		timeoutChecker.check();
	}

	void register(Connection connection) throws IOException {
		synchronized (this) {
			wakeUp();
			connection.channel.register(selector, SelectionKey.OP_READ, connection);
		}
		LOGGER.debug("{} registered.", connection);
	}

	private void enableWrite(Connection connection) {
		SelectionKey sk = keyFor(connection);
		sk.interestOps(sk.interestOps() | SelectionKey.OP_WRITE);
	}

	private void disableWrite(Connection connection) {
		SelectionKey sk = keyFor(connection);
		sk.interestOps(sk.interestOps() & (~SelectionKey.OP_WRITE));
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
