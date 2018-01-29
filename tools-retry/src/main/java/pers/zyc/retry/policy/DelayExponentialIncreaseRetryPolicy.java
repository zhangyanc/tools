package pers.zyc.retry.policy;

/**
 * 指数递增延迟策略
 *
 * @author zhangyancheng
 */
public class DelayExponentialIncreaseRetryPolicy extends DelayedRetryPolicy {
    private final long coefficient;
    private final double exponent;
    private final long maxDelay;

    public DelayExponentialIncreaseRetryPolicy(long coefficient, double exponent) {
        this.coefficient = coefficient;
        this.exponent = exponent;
        this.maxDelay = Long.MAX_VALUE;
    }

    public DelayExponentialIncreaseRetryPolicy(long coefficient, double exponent, long maxDelay) {
        checkDelayVal(coefficient, maxDelay);
        this.coefficient = coefficient;
        this.exponent = exponent;
        this.maxDelay = maxDelay;
    }

    private static void checkExponentVal(double exponent) {
        if (exponent <= 0) {
            throw new IllegalArgumentException("exponent < 0!");
        }
    }

    @Override
    protected long getDelay(int alreadyRetryCounts) {
        return Math.min((long) (Math.pow(exponent, alreadyRetryCounts) * coefficient), maxDelay);
    }
}
