package pers.zyc.tools.redis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.redis.client.exception.RedisClientException;
import pers.zyc.tools.utils.lifecycle.ThreadService;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
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
		selector.close();
	}

	@Override
	protected Runnable getRunnable() {
		return new ServiceRunnable() {

			@Override
			protected void execute() throws InterruptedException {
				try {
					select();
				} catch (IOException e) {
					throw new RedisClientException(e);
				}
			}
		};
	}

	private void select() throws InterruptedException, IOException {
		List<SelectionKey> selected = doSelect();

		for (SelectionKey sk : selected) {
			try {
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
			} catch (Exception e) {
				LOGGER.error("Connection read/write error: ", e.getMessage());
			}
		}
	}

	private List<SelectionKey> doSelect() throws InterruptedException, IOException {
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
		}
	}

	SelectionKey register(SocketChannel channel) throws IOException {
		serviceLock.lock();
		try {
			wakeUp();
			return channel.register(selector, SelectionKey.OP_READ);
		} finally {
			serviceLock.unlock();
		}
	}

	void wakeUp() {
		if (wakeUp.compareAndSet(false, true)) {
			selector.wakeup();
		}
	}
}
