package pers.zyc.tools.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

/**
 * @author zhangyancheng
 */
public class LockUtil {

	/**
	 * 在锁内执行回调
	 *
	 * @param lock 锁
	 * @param runnable 回调
	 */
	public static void runWithLock(Lock lock, Runnable runnable) {
		lock.lock();
		try {
			runnable.run();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 在锁内执行回调
	 *
	 * @param lock 锁
	 * @param callable 回调
	 * @param <R> 返回值类型
	 * @return 返回值
	 * @throws Exception 回调异常
	 */
	public static <R> R callWithLock(Lock lock, Callable<R> callable) throws Exception {
		lock.lock();
		try {
			return callable.call();
		} finally {
			lock.unlock();
		}
	}
}
