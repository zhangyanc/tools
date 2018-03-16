package pers.zyc.retry;

/**
 * 重试失败异常, 包含最后一次执行异常及重试统计
 *
 * @author zhangyancheng
 */
public class RetryFailedException extends Exception {
	private RetryStat retryStat;

	RetryFailedException(Throwable cause, RetryStat retryStat) {
		super(cause);
		this.retryStat = retryStat;
	}

	public RetryStat getRetryStat() {
		return retryStat;
	}
}
