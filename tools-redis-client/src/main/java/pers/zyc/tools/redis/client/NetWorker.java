package pers.zyc.tools.redis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.lifecycle.PeriodicService;
import pers.zyc.tools.redis.client.exception.RedisClientException;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhangyancheng
 */
class NetWorker extends PeriodicService implements EventListener<ConnectionEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetWorker.class);

	private final Selector selector = Selector.open();
	private final AtomicBoolean wakeUp = new AtomicBoolean();
	private final LinkedList<Connection> timeoutCheckConnections = new LinkedList<>();

	NetWorker() throws IOException {
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
				timeoutCheckConnections.add(connection);
				break;
			case RESPONSE_RECEIVED:
			case CONNECTION_CLOSED:
			case EXCEPTION_CAUGHT:
				timeoutCheckConnections.remove(connection);
				break;
			default:
				LOGGER.debug("OnEvent: {}", event);
		}
	}

	private SelectionKey keyFor(Connection connection) {
		return connection.channel.keyFor(selector);
	}

	private void checkTimeoutConnection() {
		Iterator<Connection> iterator = timeoutCheckConnections.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().checkTimeout()) {
				iterator.remove();
			}
		}
	}

	void register(Connection connection) throws IOException {
		synchronized (this) {
			wakeUp();
			connection.channel.register(selector, SelectionKey.OP_READ, connection);
		}
		connection.addListener(this);
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
