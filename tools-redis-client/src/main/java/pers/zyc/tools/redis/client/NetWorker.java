package pers.zyc.tools.redis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.lifecycle.PeriodicService;
import pers.zyc.tools.redis.client.exception.RedisClientException;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhangyancheng
 */
class NetWorker extends PeriodicService {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetWorker.class);

	private final Selector selector = Selector.open();
	private final AtomicBoolean wakeUp = new AtomicBoolean();
	private final LinkedList<Connection> timeoutCheckConnections = new LinkedList<>();

	NetWorker() throws IOException {
	}

	@Override
	protected void onInterrupt() {
		try {
			selector.close();
		} catch (IOException e) {
			LOGGER.warn("Close selector error!", e);
		}
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		LOGGER.error("Uncaught exception", e);
		stop();
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
		serviceLock.lock();
		try {
			wakeUp();
			connection.channel.register(selector, SelectionKey.OP_READ, connection);
		} finally {
			serviceLock.unlock();
		}
	}

	void enableWrite(Connection connection) {
		SelectionKey sk = keyFor(connection);
		sk.interestOps(sk.interestOps() | SelectionKey.OP_WRITE);

		wakeUp();
	}

	void disableWrite(Connection connection) {
		SelectionKey sk = keyFor(connection);
		sk.interestOps(sk.interestOps() & (~SelectionKey.OP_WRITE));

		timeoutCheckConnections.add(connection);
	}

	void finishRequest(Connection connection) {
		timeoutCheckConnections.remove(connection);
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
		} catch (ClosedSelectorException ignore) {
		} catch (Exception e) {
			throw new RedisClientException(e);
		}

		checkRunning();
	}

	private Set<SelectionKey> selectKey() throws InterruptedException {
		serviceLock.lockInterruptibly();
		try {
			return selector.selectedKeys();
		} finally {
			serviceLock.unlock();
		}
	}

	@Override
	protected void execute() throws InterruptedException {
		doSelect();

		Set<SelectionKey> selected = selectKey();

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
