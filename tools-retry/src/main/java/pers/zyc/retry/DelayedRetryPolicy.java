package pers.zyc.retry;

import java.util.concurrent.TimeUnit;

/**
 * @author zhangyancheng
 */
public class DelayedRetryPolicy implements RetryPolicy {
	private long retryDelay;
	private int maxRetryCount;

	private boolean useExp;
	private double baseNum;
	private long maxRetryDelay;

	@Override
	public boolean checkAndAwait(final RetryStat retryStat) throws InterruptedException {
		if (checkException(retryStat.getCause())) {
			//不是继续重试异常则不再重试
			return false;
		}

		Long nextRetryTime = checkNextRetryTime(retryStat);
		if (nextRetryTime == null) {
			//没有下次重试时间则不再重试
			return false;
		}
		await(nextRetryTime);
		return true;
	}

	/**
	 * 检查异常是否是可继续重试异常
	 * @param cause 执行异常
	 * @return 是否可继续重试异常
	 */
	protected boolean checkException(Throwable cause) {
		return true;
	}

	/**
	 * 检查下次重试时间
	 * @param retryStat 重试统计
	 * @return 下次重试时间, 为null表示不再重试
	 */
	protected Long checkNextRetryTime(RetryStat retryStat) {
		int alreadyRetryCounts = retryStat.getAlreadyRetryCounts();
		if (maxRetryCount > 0 && alreadyRetryCounts >= maxRetryCount) {
			//超过了最大重试次数
			return null;
		}
		if (useExp) {
			long delay = (long) (Math.pow(baseNum, alreadyRetryCounts) * retryDelay);
			return maxRetryDelay > 0 ? Math.min(maxRetryCount, delay) : delay;
		}
		return retryDelay;
	}

	/**
	 * 休眠到下次重试时间
	 * @param nextRetryTime 下次重试时间
	 * @throws InterruptedException 线程被中断
	 */
	protected void await(long nextRetryTime) throws InterruptedException {
		TimeUnit.MILLISECONDS.sleep(nextRetryTime - System.currentTimeMillis());
	}

	public long getRetryDelay() {
		return retryDelay;
	}

	public void setRetryDelay(long retryDelay) {
		this.retryDelay = retryDelay;
	}

	public int getMaxRetryCount() {
		return maxRetryCount;
	}

	public void setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	public boolean isUseExp() {
		return useExp;
	}

	public void setUseExp(boolean useExp) {
		this.useExp = useExp;
	}

	public double getBaseNum() {
		return baseNum;
	}

	public void setBaseNum(double baseNum) {
		this.baseNum = baseNum;
	}

	public long getMaxRetryDelay() {
		return maxRetryDelay;
	}

	public void setMaxRetryDelay(long maxRetryDelay) {
		this.maxRetryDelay = maxRetryDelay;
	}
}
