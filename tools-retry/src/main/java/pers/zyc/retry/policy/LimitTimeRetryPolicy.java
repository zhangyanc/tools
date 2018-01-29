package pers.zyc.retry.policy;

import pers.zyc.retry.RetryPolicy;
import pers.zyc.retry.RetryStat;

/**
 * 限制总重试时间
 *
 * @author zhangyancheng
 */
public class LimitTimeRetryPolicy extends LimitedRetryPolicy {
    private final long time;

    public LimitTimeRetryPolicy(RetryPolicy retryPolicy, long time) {
        super(retryPolicy);
        this.time = time;
    }

    @Override
    protected boolean hasNext(final RetryStat retryStat) {
        return retryStat.loopTime() - retryStat.startTime() < time;
    }
}
