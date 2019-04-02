package pers.zyc.tools.redis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.utils.GeneralThreadFactory;
import pers.zyc.tools.utils.lifecycle.Service;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhangyancheng
 */
class NetWorkGroup extends Service {
	private static final Logger LOGGER = LoggerFactory.getLogger(NetWorkGroup.class);
	private static final AtomicInteger GROUP_NUMBERS = new AtomicInteger();

	private final NetWorker[] netWorkers;
	private final GeneralThreadFactory threadFactory;
	private final AtomicInteger chooseIndexer = new AtomicInteger();

	NetWorkGroup(int netWorkers) {
		if (netWorkers <= 0) {
			throw new IllegalArgumentException(String.format("netWorkers: %d (expected: > 0)", netWorkers));
		}
		this.netWorkers = new NetWorker[netWorkers];
		threadFactory = new GeneralThreadFactory("NWG-" + GROUP_NUMBERS.incrementAndGet() + "-NW-");
		threadFactory.setExceptionHandler(new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				LOGGER.error(t + "uncaught exception", e);
				NetWorker netWorker = match(t);
				if (netWorker == null) {
					LOGGER.error("NewWorker unmatched, {}", t);
					return;
				}
				netWorker.stop();
				LOGGER.warn("NetWorker[{}] stopped", netWorker.getName());
			}
		});
	}

	@Override
	protected void doStart() throws Exception {
		try {
			for (int i = 0; i < netWorkers.length; i++) {
				NetWorker netWorker = new NetWorker();
				netWorker.setThreadFactory(threadFactory);
				netWorker.start();
				this.netWorkers[i++] = netWorker;
			}
		} catch (IOException e) {
			closeWorkers(this.netWorkers);
			throw e;
		}
	}

	@Override
	protected void doStop() throws Exception {
		closeWorkers(this.netWorkers);
	}

	NetWorker next() {
		return netWorkers[chooseIndexer.getAndIncrement() % netWorkers.length];
	}

	boolean inNetworking() {
		return match(Thread.currentThread()) != null;
	}

	private NetWorker match(Thread t) {
		for (NetWorker netWorker : netWorkers) {
			if (netWorker.isServiceThread(t)) {
				return netWorker;
			}
		}
		return null;
	}

	private static void closeWorkers(NetWorker[] netWorkers) {
		for (NetWorker netWorker : netWorkers) {
			if (netWorker != null) {
				netWorker.stop();
			}
		}
	}
}
