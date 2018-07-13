package pers.zyc.tools.utils.retry;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangyancheng
 */
public class BaseRetryPolicy implements RetryPolicy {
	/**
	 * 重试间隔(ms), 如果不使用指数增长方式那么每次重试的间隔都是该值,
	 * 如果使用指数增长的方式该值作为计算系数
	 */
	private long retryDelay;
	/**
	 * 最大重试次数, 默认没有最大次数限制
	 */
	private int maxRetryTimes;

	/**
	 * 是否指数增长间隔
	 *
	 * 计算方式: Math.pow(baseNum, alreadyRetryCounts) * retryDelay
	 * 如果底数设置为2, 重试间隔为1000, 则
	 * 		第一次重试: 1 * 1000 = 1000
	 * 		第二次重试: 2 * 1000 = 2000
	 * 		第三次重试: 4 * 1000 = 4000
	 */
	private boolean useExp;
	/**
	 * 底数
	 */
	private double baseNum = 1.0;
	/**
	 * 最大重试间隔(ms), 默认没有最大间隔限制(如果是指数增长的总是按照指数增长值)
	 */
	private long maxRetryDelay;

	@Override
	public Boolean handleException(Throwable cause, Callable<?> callable) {
		//如果重试任务本身实现了异常处理则返回其处理结果, 不然返回真表示接受所有类型异常
		return !(callable instanceof RetryExceptionHandler) ||
				((RetryExceptionHandler) callable).handleException(cause, callable);
	}

	@Override
	public boolean awaitToRetry(final RetryStat retryStat) throws InterruptedException {
		int alreadyRetryTimes = retryStat.getAlreadyRetryTimes();
		if (maxRetryTimes > 0 && alreadyRetryTimes >= maxRetryTimes) {
			//超过了最大重试次数
			return false;
		}

		long awaitTime;
		if (!useExp) {
			awaitTime = retryDelay;
		} else {
			awaitTime = (long) (Math.pow(baseNum, alreadyRetryTimes) * retryDelay);
			if (maxRetryDelay > 0) {
				awaitTime = Math.min(maxRetryDelay, awaitTime);
			}
		}
		//等待直到下次重试
		return await(awaitTime);
	}

	/**
	 * 休眠到下次重试时间
	 * @param awaitTime 等待时间
	 * @throws InterruptedException 线程被中断
	 */
	protected boolean await(long awaitTime) throws InterruptedException {
		TimeUnit.MILLISECONDS.sleep(awaitTime);
		return true;
	}

	public long getRetryDelay() {
		return retryDelay;
	}

	public void setRetryDelay(long retryDelay) {
		this.retryDelay = retryDelay;
	}

	public int getMaxRetryTimes() {
		return maxRetryTimes;
	}

	public void setMaxRetryTimes(int maxRetryTimes) {
		this.maxRetryTimes = maxRetryTimes;
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
