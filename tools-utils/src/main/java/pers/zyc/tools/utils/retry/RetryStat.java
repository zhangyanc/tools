package pers.zyc.tools.utils.retry;

import pers.zyc.tools.utils.SystemMillis;

/**
 * 重试统计
 *
 * @author zhangyancheng
 * @see RetryLoop
 * @see RetryPolicy
 */
public class RetryStat {
	/**
	 * 重试任务开始时间
	 */
	private long startTime = SystemMillis.current();
	/**
	 * 已经重试的次数
	 */
	private int alreadyRetryTimes;
	/**
	 * 首次重试时间
	 */
	private long firstRetryTime;
	/**
	 * 最后一次重试时间
	 */
	private long lastRetryTime;

	/**
	 * 重试任务执行失败
	 */
	void retry() {
		lastRetryTime = SystemMillis.current();
		if (firstRetryTime == 0) {
			firstRetryTime = lastRetryTime;
		}
		alreadyRetryTimes++;
	}

	public long getStartTime() {
		return startTime;
	}

	public int getAlreadyRetryTimes() {
		return alreadyRetryTimes;
	}

	public long getLastRetryTime() {
		return lastRetryTime;
	}

	public long getFirstRetryTime() {
		return firstRetryTime;
	}

	@Override
	public String toString() {
		return "RetryStat{" +
				"startTime=" + startTime +
				", alreadyRetryTimes=" + alreadyRetryTimes +
				", firstRetryTime=" + firstRetryTime +
				", lastRetryTime=" + lastRetryTime +
				'}';
	}
}
