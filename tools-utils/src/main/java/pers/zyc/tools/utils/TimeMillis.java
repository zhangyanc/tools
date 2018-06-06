package pers.zyc.tools.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 系统毫秒数
 *
 * @author zhangyancheng
 */
public final class TimeMillis {

	private static final AtomicLong TIME_MILLIS = new AtomicLong(System.currentTimeMillis());

	static {
		Executors.newSingleThreadScheduledExecutor(new NameThreadFactory("TIME_MILLIS", true))
				 .scheduleAtFixedRate(new Runnable() {

					 @Override
					 public void run() {
						 TIME_MILLIS.set(System.currentTimeMillis());
					 }
				 }, 0, 1, TimeUnit.MILLISECONDS);
	}

	public static long get() {
		return TIME_MILLIS.get();
	}

	public static long now() {
		return System.currentTimeMillis();
	}
}
