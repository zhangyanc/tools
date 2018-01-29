package pers.zyc.retry.policy;

import pers.zyc.retry.RetryPolicy;
import pers.zyc.retry.RetryStat;

/**
 * 延迟控制的重试策略
 *
 * @author zhangyancheng
 */
public abstract class DelayedRetryPolicy implements RetryPolicy {

    @Override
    public Long nextRetryTime(RetryStat retryStat) {
        return retryStat.loopTime() + getDelay(retryStat.alreadyRetryCounts());
    }

    /**
     * @param alreadyRetryCounts 当前重试次数
     * @return 此轮延迟时间
     */
    protected abstract long getDelay(int alreadyRetryCounts);

    /**
     * 延迟必须为正数
     *
     * @param delays 延迟
     */
    static void checkDelayVal(long... delays) {
        for (long delay : delays) {
            if (delay <= 0) {
                throw new IllegalArgumentException("delay < 0!");
            }
        }
    }
}
