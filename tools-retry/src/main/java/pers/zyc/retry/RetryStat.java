package pers.zyc.retry;

/**
 * @author zhangyancheng
 */
public class RetryStat {
	private long startTime;
	private int alreadyRetryCounts;
	private long loopTime;
	private Throwable cause;

	void loop(Throwable cause) {
		this.cause = cause;
		loopTime = System.currentTimeMillis();
		//根据startTime区分是否为首次异常, 是首次异常则记录启动为重试启动时间, 否则增加重试次数
		if (startTime == 0) {
			startTime = loopTime;
		} else {
			alreadyRetryCounts++;
		}
	}

	public long getStartTime() {
		return startTime;
	}

	public int getAlreadyRetryCounts() {
		return alreadyRetryCounts;
	}

	public long getLoopTime() {
		return loopTime;
	}

	public Throwable getCause() {
		return cause;
	}
}
