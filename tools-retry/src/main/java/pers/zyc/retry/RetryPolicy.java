package pers.zyc.retry;


/**
 * 重试策略
 *
 * @author zhangyancheng
 */
public interface RetryPolicy extends RetryExceptionHandler {


	/**
	 * 等待直到下次重试开始
	 * @param retryStat 重试统计
	 * @return 是否继续重试
	 */
	boolean awaitToRetry(RetryStat retryStat) throws InterruptedException;
}
