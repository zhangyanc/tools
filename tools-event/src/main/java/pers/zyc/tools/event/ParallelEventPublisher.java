package pers.zyc.tools.event;

import pers.zyc.tools.utils.NameThreadFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 并行发布(在异步线程池中发布事件)
 *
 * @author zhangyancheng
 */
public class ParallelEventPublisher extends SerialEventPublisher implements ParallelPublisher {
    private final static AtomicInteger counter = new AtomicInteger();

    /**
     * 默认使用缓存线程池, 所有事件发布都完全并行化
     */
    private ExecutorService parallelExecutor = Executors.newCachedThreadPool(
            new NameThreadFactory("ParallelPublisher_CachedPool_" + counter.getAndIncrement()));

    public ParallelEventPublisher() {
    }

    public ParallelEventPublisher(ExecutorService parallelExecutor) {
        this.parallelExecutor = Objects.requireNonNull(parallelExecutor);
    }

    public ParallelEventPublisher(PublishExceptionHandler publishExceptionHandler) {
        super(publishExceptionHandler);
    }

    public ParallelEventPublisher(ExecutorService parallelExecutor, PublishExceptionHandler publishExceptionHandler) {
        super(publishExceptionHandler);
        this.parallelExecutor = Objects.requireNonNull(parallelExecutor);
    }

    @Override
    public <E> void publish(final E event, final EventListener<E> listener) {
        parallelExecutor.execute(new Runnable() {
            @Override
            public void run() {
                ParallelEventPublisher.super.publish(event, listener);
            }
        });
    }

    @Override
    public ExecutorService getParallelExecutor() {
        return parallelExecutor;
    }

    @Override
    public void setParallelExecutor(ExecutorService parallelExecutor) {
        this.parallelExecutor = Objects.requireNonNull(parallelExecutor);
    }
}
