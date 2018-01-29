package pers.zyc.retry.policy;

/**
 * 轮询延迟策略
 *
 * @author zhangyancheng
 */
public class DelayRoundRetryPolicy extends DelayedRetryPolicy {
    private final long[] delays;

    public DelayRoundRetryPolicy(long... delays) {
        if (delays.length < 1) {
            throw new IllegalArgumentException("delays len < 1!");
        }
        checkDelayVal(delays);
        this.delays = delays;
    }

    @Override
    protected long getDelay(int alreadyRetryCounts) {
        //round
        return delays[alreadyRetryCounts % delays.length];
    }

    public static DelayedRetryPolicy createFixedDelayRetryPolicy(long delay) {
        return new DelayRoundRetryPolicy(delay);
    }
}
