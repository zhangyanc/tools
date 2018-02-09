package pers.zyc.tools.event;

import java.util.concurrent.ExecutorService;

/**
 * 并行发布器
 *
 * @author zhangyancheng
 */
public interface ParallelPublisher extends EventPublisher {

    ExecutorService getParallelExecutor();

    /**
     * 设置并行发布线程池
     */
    void setParallelExecutor(ExecutorService parallelExecutor);
}
