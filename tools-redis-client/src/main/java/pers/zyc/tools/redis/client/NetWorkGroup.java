package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.exception.RedisClientException;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhangyancheng
 */
class NetWorkGroup implements Closeable {

	private final NetWorker[] netWorkers;
	private final ConcurrentMap<Integer, NetWorker> netWorkerMap = new ConcurrentHashMap<>();

	NetWorkGroup(int netWorkers) {
		if (netWorkers <= 0) {
			throw new IllegalArgumentException(String.format("netWorkers: %d (expected: > 0)", netWorkers));
		}
		this.netWorkers = new NetWorker[netWorkers];
		try {
			for (int i = 0; i < netWorkers; i++) {
				NetWorker netWorker = new NetWorker();
				netWorker.start();
				this.netWorkers[i++] = netWorker;
			}
		} catch (IOException e) {
			closeWorkers(this.netWorkers);
			throw new RedisClientException("Worker start error!");
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

	NetWorker getNetWorker(int connId) {
		NetWorker netWorker = netWorkerMap.get(connId);
		if (netWorker == null) {
			netWorker = netWorkers[connId % netWorkers.length];
			netWorkerMap.put(connId, netWorker);
		}
		return netWorker;
	}
}
