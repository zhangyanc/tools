package pers.zyc.retry.policy;

import pers.zyc.retry.RetryPolicy;
import pers.zyc.retry.RetryStat;

import java.util.Objects;

/**
 * 有限制的重试策略
 *
 * @author zhangyancheng
 */
public abstract class LimitedRetryPolicy implements RetryPolicy {
    protected final RetryPolicy retryPolicy;

    public LimitedRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = Objects.requireNonNull(retryPolicy);
    }

    @Override
    public Long nextRetryTime(RetryStat retryStat) {
        return hasNext(retryStat) ? retryPolicy.nextRetryTime(retryStat) : null;
    }

    protected abstract boolean hasNext(final RetryStat retryStat);
}
