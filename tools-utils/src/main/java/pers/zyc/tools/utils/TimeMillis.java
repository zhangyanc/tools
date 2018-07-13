package pers.zyc.tools.utils;

import pers.zyc.tools.utils.lifecycle.ThreadService;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 系统毫秒数
 *
 * @author zhangyancheng
 */
public class TimeMillis extends ThreadService {

	public static final TimeMillis INSTANCE;

	static {
		INSTANCE = new TimeMillis();
		INSTANCE.start();
	}

	private TimeMillis() {
		setThreadFactory(new GeneralThreadFactory("TIME_MILLIS") {
			{
				setDaemon(true);
				setPriority(Thread.MIN_PRIORITY);
			}
		});
	}

	private final AtomicLong timeMillis = new AtomicLong(System.currentTimeMillis());

	@Override
	protected Runnable getRunnable() {
		return new ServiceRunnable() {

			@Override
			protected long getInterval() {
				return 1;
			}

			@Override
			protected void execute() throws InterruptedException {
				timeMillis.set(current());
			}
		};
	}

	public long get() {
		if (isRunning()) {
			return timeMillis.get();
		}
		return current();
	}

	public long current() {
		return System.currentTimeMillis();
	}
}
