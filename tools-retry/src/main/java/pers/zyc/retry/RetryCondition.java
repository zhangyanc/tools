package pers.zyc.retry;

/**
 * @author zhangyancheng
 */
public interface RetryCondition {
	/**
	 * @return 重试条件是否满足
	 */
	boolean check();

	/**
	 * @return wait mutex
	 */
	Object getMutex();
}
