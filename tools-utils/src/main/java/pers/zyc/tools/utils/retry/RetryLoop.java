package pers.zyc.tools.utils.retry;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * @author zhangyancheng
 */
public class RetryLoop {

	/**
	 * 按照指定的重试策略执行重试逻辑
	 *
	 * @param retryAble 重试任务
	 * @param retryPolicy 重试策略, 如果为null表示不进行重试
	 * @param <V> 执行结果泛型
	 * @return 任务执行结果
	 * @throws InterruptedException 重试过程中线程被中断
	 * @throws RetryFailedException 重试失败
	 */
	public static <V> V execute(Callable<V> retryAble,
								RetryPolicy retryPolicy)
			throws InterruptedException, RetryFailedException {

		Objects.requireNonNull(retryAble);
		final RetryStat retryStat = new RetryStat();

		Throwable cause;//记录异常
		while (true) {
			try {
				return retryAble.call();
			} catch (InterruptedException Interrupted) {
				throw Interrupted;
			} catch (Throwable throwable) {
				cause = throwable;
				if (retryPolicy == null ||
					//非可继续重试异常
					!retryPolicy.handleException(cause, retryAble) ||
					//根据重试统计等待直到下次重试
					!retryPolicy.awaitToRetry(retryStat)) {
					break;
				}
				//标记一轮重试
				retryStat.retry();
			}
		}
		throw new RetryFailedException(cause, retryStat);
	}
}
