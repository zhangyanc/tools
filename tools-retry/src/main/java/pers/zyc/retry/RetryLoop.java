package pers.zyc.retry;

import java.util.concurrent.Callable;

/**
 * @author zhangyancheng
 */
public class RetryLoop {

	/**
	 * @param retryAble 重试任务
	 * @param retryPolicy 重试策略
	 * @param <V> 重试结果泛型
	 * @return 重试结果
	 * @throws InterruptedException 重试过程中线程被中断
	 * @throws RetryFailedException 重试失败
	 */
	public static <V> V execute(Callable<V> retryAble,
								RetryPolicy retryPolicy)
			throws InterruptedException, RetryFailedException {

		RetryStat retryStat = new RetryStat();

		while (true) {
			try {
				return retryAble.call();
			} catch (Throwable cause) {
				if (retryPolicy == null) {
					//没有重试策略不进行重试
					break;
				}
				retryStat.loop(cause);
				if (!retryPolicy.checkAndAwait(retryStat)) {
					//不再继续重试
					break;
				}
			}
		}
		throw new RetryFailedException(retryStat);
	}
}
