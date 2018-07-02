package pers.zyc.tools.redis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.lifecycle.PeriodicService;
import pers.zyc.tools.utils.Pair;
import pers.zyc.tools.utils.TimeMillis;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author zhangyancheng
 */
class TimeoutGuarder extends PeriodicService implements EventListener<ConnectionEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeoutGuarder.class);
	private static final long CHECK_PERIOD = 1000;

	private final Timeout timeout;

	TimeoutGuarder(final int requestTimeout) {
		this.timeout = requestTimeout <= 0 ? NON_TIMEOUT : new Timeout() {

			private final List<TimeoutInfo> timeoutInfoList = new CopyOnWriteArrayList<>();

			@Override
			public void add(Connection connection) {
				timeoutInfoList.add(new TimeoutInfo(connection, TimeMillis.get() + requestTimeout));
			}

			@Override
			public void remove(Connection connection) {
				timeoutInfoList.remove(connection);
			}

			@Override
			public void check() {
				Iterator<TimeoutInfo> iterator = timeoutInfoList.iterator();
				while (iterator.hasNext()) {
					TimeoutInfo entry = iterator.next();
					if (entry.value() <= TimeMillis.get()) {
						entry.key().timeout();
						iterator.remove();
					}
				}
			}
		};
	}

	@Override
	protected long period() {
		return CHECK_PERIOD;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		super.uncaughtException(t, e);
		LOGGER.error("Uncaught exception, TimeoutChecker stopped.", e);
	}

	@Override
	protected void execute() throws InterruptedException {
		timeout.check();
	}

	@Override
	public void onEvent(ConnectionEvent event) {
		LOGGER.debug("TimeoutChecker: {}", event);

		Connection connection = event.getSource();
		switch (event.eventType) {
			case REQUEST_SET:
				timeout.add(connection);
				break;
			case RESPONSE_RECEIVED:
			case CONNECTION_CLOSED:
			case EXCEPTION_CAUGHT:
				timeout.remove(connection);
		}
	}

	private static class TimeoutInfo extends Pair<Connection, Long> {
		TimeoutInfo(Connection connection, long timeoutLine) {
			key(connection);
			value(timeoutLine);
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Connection ? obj == key() : super.equals(obj);
		}
	}

	private interface Timeout {
		void add(Connection connection);

		void remove(Connection connection);

		void check();
	}

	private static Timeout NON_TIMEOUT = new Timeout() {

		@Override
		public void add(Connection connection) {
		}

		@Override
		public void remove(Connection connection) {
		}

		@Override
		public void check() {
		}
	};
}
