package pers.zyc.tools.event;

import pers.zyc.tools.utils.NameThreadFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zhangyancheng
 */
public class ParallelInvoker extends SerialInvoker {

    private static final ExecutorService DEFAULT_EXECUTOR =
            Executors.newCachedThreadPool(new NameThreadFactory("ParallelInvoker"));

    private ExecutorService executor;

    public ParallelInvoker() {
        this(DEFAULT_EXECUTOR);
    }

    public ParallelInvoker(ExecutorService executor) {
        this.executor = Objects.requireNonNull(executor);
    }

    @Override
    public <E> void invoke(final E event, final EventListener<E> listener) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                ParallelInvoker.super.invoke(event, listener);
            }
        });
    }
}
