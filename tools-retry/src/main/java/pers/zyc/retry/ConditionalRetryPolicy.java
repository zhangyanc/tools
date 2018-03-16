package pers.zyc.retry;

import pers.zyc.tools.utils.TimeMillis;

import java.util.Objects;

/**
 * "条件决定"重试策略, 可被提前唤醒
 * @author zhangyancheng
 */
public class ConditionalRetryPolicy extends BaseRetryPolicy {
	/**
	 * 重试条件
	 */
	private final RetryCondition retryCondition;

	public ConditionalRetryPolicy(RetryCondition retryCondition) {
		this.retryCondition = Objects.requireNonNull(retryCondition);
	}

	@Override
	protected boolean await(long nextRetryTime) throws InterruptedException {
		long now;
		synchronized (retryCondition.getMutex()) {
			while (!retryCondition.check() && (now = TimeMillis.get()) < nextRetryTime) {
				retryCondition.wait(nextRetryTime - now);
			}
			//条件到达或者已到retry time
		}
		//是否继续重试由retry condition决定
		return retryCondition.check();
	}
}
