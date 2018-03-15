package pers.zyc.retry;

/**
 * @author zhangyancheng
 */
public interface RetryPolicy {

	/**
	 * 等待直到下次重试开始
	 * @param retryStat 重试统计
	 * @return 是否继续重试
	 */
	boolean checkAndAwait(RetryStat retryStat) throws InterruptedException;
}
