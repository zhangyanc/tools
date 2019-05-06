package pers.zyc.tools.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

/**
 * @author zhangyancheng
 */
public class Locks {

	/**
	 * 在锁内执行回调
	 *
	 * @param lock 锁
	 * @param runnable 回调
	 */
	public static void run(Lock lock, Runnable runnable) {
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
	 * @param action 回调
	 * @throws E 回调异常
	 */
	public static <E  extends Exception> void execute(Lock lock, RunAction<E> action) throws E {
		lock.lock();
		try {
			action.run();
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
	public static <R> R call(Lock lock, Callable<R> callable) throws Exception {
		lock.lock();
		try {
			return callable.call();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 在锁内执行回调
	 *
	 * @param lock 锁
	 * @param callAction 回调
	 * @param <R> 返回值类型
	 * @param <E> 回调异常类型
	 * @return 返回值
	 * @throws E 异常
	 */
	public static <R, E extends Exception> R execute(Lock lock, CallAction<R, E> callAction) throws E {
		lock.lock();
		try {
			return callAction.call();
		} finally {
			lock.unlock();
		}
	}
}
