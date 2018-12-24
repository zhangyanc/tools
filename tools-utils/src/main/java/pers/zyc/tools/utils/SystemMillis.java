package pers.zyc.tools.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhangyancheng
 */
public class SystemMillis {

	private SystemMillis() {
	}

	static {
		Executors.newSingleThreadScheduledExecutor(new GeneralThreadFactory("SYSTEM_MILLIS") {
			{
				// 设置为守护线程, 无需显式退出
				setDaemon(true);
				setPriority(Thread.MIN_PRIORITY);
			}
		}).scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				CURRENT_SYSTEM_MILLIS.set(System.currentTimeMillis());
			}
		}, 0, 1, TimeUnit.MILLISECONDS);
	}

	private static final AtomicLong CURRENT_SYSTEM_MILLIS = new AtomicLong(System.currentTimeMillis());

	public static long current() {
		return CURRENT_SYSTEM_MILLIS.get();
	}
}
