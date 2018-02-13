package pers.zyc.tools.event;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * 在异步线程池执行事件回调
 * @author zhangyancheng
 */
public class AsyncEventMulticaster extends AbstractEventMulticaster {
    private ExecutorService multicastExecutor;

    public AsyncEventMulticaster(ExecutorService multicastExecutor) {
        this.multicastExecutor = Objects.requireNonNull(multicastExecutor);
    }

    public AsyncEventMulticaster(ExecutorService multicastExecutor,
                                 MulticastExceptionHandler multicastExceptionHandler) {
        this(multicastExecutor);
        setDeliverExceptionHandler(multicastExceptionHandler);
    }

    @Override
    public ExecutorService getMulticastExecutor() {
        return multicastExecutor;
    }
}
