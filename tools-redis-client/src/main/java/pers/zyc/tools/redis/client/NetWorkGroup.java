package pers.zyc.tools.redis.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhangyancheng
 */
class NetWorkGroup implements Closeable {

	private final NetWorker[] netWorkers;
	private final AtomicInteger chooseIndexer = new AtomicInteger();

	NetWorkGroup(int netWorkers) {
		if (netWorkers <= 0) {
			throw new IllegalArgumentException(String.format("netWorkers: %d (expected: > 0)", netWorkers));
		}
		this.netWorkers = new NetWorker[netWorkers];
		try {
			for (int i = 0; i < netWorkers; i++) {
				NetWorker netWorker = new NetWorker(1);
				netWorker.start();
				this.netWorkers[i++] = netWorker;
			}
		} catch (IOException e) {
			closeWorkers(this.netWorkers);
			throw new IllegalStateException("Worker start error!");
		}
	}

	private static void closeWorkers(NetWorker[] netWorkers) {
		for (NetWorker netWorker : netWorkers) {
			if (netWorker != null) {
				netWorker.stop();
			}
		}
	}

	@Override
	public void close() {
		closeWorkers(this.netWorkers);
	}

	void register(Connection connection) throws IOException {
		next().register(connection);
	}

	private NetWorker next() {
		return netWorkers[chooseIndexer.getAndIncrement() % netWorkers.length];
	}
}
