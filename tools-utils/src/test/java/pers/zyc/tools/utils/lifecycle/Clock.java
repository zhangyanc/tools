package pers.zyc.tools.utils.lifecycle;

import java.util.concurrent.ThreadFactory;

/**
 * @author zhangyancheng
 */
public class Clock extends ThreadService {

	private Clock() {
		setThreadFactory(new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r, "Clock");
				thread.setDaemon(true);
				return thread;
			}
		});
	}

	private volatile long timeMillis = 0;

	private static final Clock CLOCK = new Clock();

	public static Clock getClock() {
		return CLOCK;
	}

	@Override
	protected void doStart() {
		//标记一下当前时间, 不然start后可能读到0
		timeMillis = System.currentTimeMillis();
	}

	@Override
	protected ServiceRunnable getRunnable() {
		return new ServiceRunnable() {

			@Override
			protected long getInterval() {
				return 1;
			}

			@Override
			protected void execute() throws InterruptedException {
				timeMillis = System.currentTimeMillis();
			}
		};
	}

	public long now() {
		checkRunning();
		return timeMillis;
	}

	public static void main(String[] args) throws InterruptedException {
		Clock clock = Clock.getClock();
		try {
			clock.now();
			throw new RuntimeException();
		} catch (ServiceException.NotRunningException ignored) {
		}
		if (clock.getState() != ServiceState.NEW) {
			throw new RuntimeException();
		}

		clock.start();
		clock.checkRunning();
		clock.now();

		for (int i = 0; i < 10; i++) {
			long cn = clock.now(), ct = System.currentTimeMillis();
			if (cn > ct) {
				throw new RuntimeException();
			}

			System.out.println(cn + " <= " + ct);
			Thread.sleep((long) ((Math.random() + 0.0001) * 10000));
		}

		clock.stop();
		if (clock.getState() != ServiceState.STOPPED) {
			throw new RuntimeException();
		}
		try {
			clock.now();
			throw new RuntimeException();
		} catch (ServiceException.NotRunningException ignored) {
		}
	}
}
