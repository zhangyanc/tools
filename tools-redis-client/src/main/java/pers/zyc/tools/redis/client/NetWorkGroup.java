package pers.zyc.tools.redis.client;

import pers.zyc.tools.lifecycle.Service;
import pers.zyc.tools.redis.client.exception.RedisClientException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhangyancheng
 */
class NetWorkGroup extends Service {
	private final NetWorker[] netWorkers;
	private final AtomicInteger chooseIndexer = new AtomicInteger();

	NetWorkGroup(int netWorkers) {
		if (netWorkers <= 0) {
			throw new IllegalArgumentException(String.format("netWorkers: %d (expected: > 0)", netWorkers));
		}
		this.netWorkers = new NetWorker[netWorkers];
	}

	@Override
	protected void doStart() {
		try {
			for (int i = 0; i < netWorkers.length; i++) {
				NetWorker netWorker = new NetWorker();
				netWorker.start();
				this.netWorkers[i++] = netWorker;
			}
		} catch (IOException e) {
			closeWorkers(this.netWorkers);
			throw new RedisClientException("Worker start error!");
		}
	}

	@Override
	protected void doStop() throws Exception {
		closeWorkers(this.netWorkers);
	}

	NetWorker next() {
		return netWorkers[chooseIndexer.getAndIncrement() % netWorkers.length];
	}

	private static void closeWorkers(NetWorker[] netWorkers) {
		for (NetWorker netWorker : netWorkers) {
			if (netWorker != null) {
				netWorker.stop();
			}
		}
	}
}
