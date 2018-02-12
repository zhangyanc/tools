package pers.zyc.tools.event;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * 在异步线程池中递送事件
 *
 * @author zhangyancheng
 */
public class AsyncDelivery extends BaseDelivery implements IAsyncDelivery {

    private final ExecutorService deliverTaskExecutor;

    public AsyncDelivery(ExecutorService deliverTaskExecutor) {
        this.deliverTaskExecutor = Objects.requireNonNull(deliverTaskExecutor);
    }

    public AsyncDelivery(DeliverExceptionHandler deliverExceptionHandler,
                         ExecutorService deliverTaskExecutor) {

        this(deliverTaskExecutor);
        setDeliverExceptionHandler(deliverExceptionHandler);
    }

    @Override
    public ExecutorService getDeliverTaskExecutor() {
        return deliverTaskExecutor;
    }
}
