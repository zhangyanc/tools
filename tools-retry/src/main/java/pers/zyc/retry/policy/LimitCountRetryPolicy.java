package pers.zyc.retry.policy;

import pers.zyc.retry.RetryPolicy;
import pers.zyc.retry.RetryStat;

/**
 * 限制总重试次数
 *
 * @author zhangyancheng
 */
public class LimitCountRetryPolicy extends LimitedRetryPolicy {
    private final int maxCounts;

    public LimitCountRetryPolicy(RetryPolicy retryPolicy, int maxCounts) {
        super(retryPolicy);
        this.maxCounts = maxCounts;
    }

    @Override
    protected boolean hasNext(final RetryStat retryStat) {
        return retryStat.alreadyRetryCounts() < maxCounts;
    }
}
