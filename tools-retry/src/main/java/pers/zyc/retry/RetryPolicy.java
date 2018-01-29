package pers.zyc.retry;

import java.io.Serializable;

/**
 * 重试策略
 *
 * @author zhangyancheng
 */
public interface RetryPolicy extends Serializable {

    /**
     * 根据上下文或许下次重试时间
     *
     * @param retryStat 重试上下文
     * @return 下次重试时间, 如果为null表示不再重试
     */
    Long nextRetryTime(RetryStat retryStat);
}
