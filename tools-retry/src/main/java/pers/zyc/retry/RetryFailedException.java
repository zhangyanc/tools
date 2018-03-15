package pers.zyc.retry;

/**
 * 重试失败异常
 *
 * @author zhangyancheng
 */
public class RetryFailedException extends Exception {
	private RetryStat retryStat;

	public RetryFailedException(RetryStat retryStat) {
		this.retryStat = retryStat;
	}

	public RetryStat getRetryStat() {
		return retryStat;
	}

	@Override
	public Throwable getCause() {
		return retryStat.getCause();
	}
}
