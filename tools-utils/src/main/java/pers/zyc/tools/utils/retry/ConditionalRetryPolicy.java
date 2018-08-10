package pers.zyc.tools.utils.retry;

import java.util.Objects;

/**
 * "条件检查"重试策略, 可被提前唤醒
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
	protected boolean await(long awaitTime) throws InterruptedException {
		synchronized (retryCondition.getMutex()) {
			while (!retryCondition.check() && awaitTime > 0) {
				long now = System.currentTimeMillis();
				retryCondition.getMutex().wait(awaitTime);
				awaitTime -= System.currentTimeMillis() - now;
			}
			//条件到达或者已到retry time
		}
		//是否继续重试由retry condition决定
		return retryCondition.check();
	}
}
