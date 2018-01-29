package pers.zyc.retry;

import pers.zyc.retry.exception.RetryCanceledException;
import pers.zyc.retry.exception.RetryFailedException;
import pers.zyc.retry.exception.RunOutOfPolicyException;

/**
 * @author zhangyancheng
 */
public final class RetryLoop {

    /**
     * 执行重试
     *
     * @param retryAble 重试任务
     * @param retryPolicy 重试策略 为null表示为无需重试任务
     * @return 重试成功结果或者默认值
     * @throws NullPointerException retryAble is null
     * @throws InterruptedException 线程被打断
     * @throws RetryCanceledException 重试结束之前, 任务被取消
     * @throws RunOutOfPolicyException 重试结束之前, 策略耗尽
     */
    public static <R> R execute(RetryAble<R> retryAble, RetryPolicy retryPolicy)
            throws InterruptedException, RetryFailedException {

        Long nextRetryTime;
        RetryStat retryStat = new RetryStat();
        //retry loop
        while (retryAble.isNotCanceled()) {
            try {
                return retryAble.call();
            } catch (InterruptedException | RetryCanceledException ie) {
                throw ie;
            } catch (Exception e) {
                retryStat.loop(e);
                //是否接着重试
                if (retryPolicy == null || !retryAble.onException(e)) {
                    break;
                }
                nextRetryTime = retryPolicy.nextRetryTime(retryStat);
                if (nextRetryTime == null) {
                    break;
                }
                retryAble.waitUntil(nextRetryTime);
            }
        }
        return retryAble.checkDefault();
    }
}
