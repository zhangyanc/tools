package pers.zyc.tools.redis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.redis.client.exception.RedisClientException;
import pers.zyc.tools.utils.lifecycle.ThreadService;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhangyancheng
 */
class NetWorker extends ThreadService {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetWorker.class);

	private final Selector selector = Selector.open();
	private final AtomicBoolean wakeUp = new AtomicBoolean();

	NetWorker() throws IOException {
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();

		try {
			selector.close();
		} catch (IOException e) {
			LOGGER.warn("Close selector error!", e);
		}
	}

	@Override
	protected Runnable getRunnable() {
		return new ServiceRunnable() {

			@Override
			protected long getInterval() {
				return 0;
			}

			@Override
			protected void execute() throws InterruptedException {
				select();
			}
		};
	}

	private void select() throws InterruptedException {
		List<SelectionKey> selected = doSelect();

		for (SelectionKey sk : selected) {
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

	private List<SelectionKey> doSelect() throws InterruptedException {
		List<SelectionKey> selected = new ArrayList<>();
		try {
			selector.select(1000);

			if (!isRunning()) {
				return selected;
			}
			if (wakeUp.getAndSet(false)) {
				selector.selectNow();
			}

			serviceLock.lockInterruptibly();
			try {
				Set<SelectionKey> sks = selector.selectedKeys();
				if (!sks.isEmpty()) {
					selected.addAll(sks);
					sks.clear();
				}
				return selected;
			} finally {
				serviceLock.unlock();
			}
		} catch (ClosedSelectorException cse) {
			return selected;
		} catch (InterruptedException re) {
			throw re;
		} catch (Exception e) {
			throw new RedisClientException(e);
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

	private SelectionKey keyFor(Connection connection) {
		return connection.channel.keyFor(selector);
	}

	void enableWrite(Connection connection) {
		SelectionKey sk = keyFor(connection);
		sk.interestOps(sk.interestOps() | SelectionKey.OP_WRITE);

		wakeUp();
	}

	void disableWrite(Connection connection) {
		SelectionKey sk = keyFor(connection);
		sk.interestOps(sk.interestOps() & (~SelectionKey.OP_WRITE));
	}

	private void wakeUp() {
		if (wakeUp.compareAndSet(false, true)) {
			selector.wakeup();
		}
	}
}
