package pers.zyc.tools.event;

import java.util.concurrent.Executor;

/**
 * 同步递送(在deliver调用线程中发布事件)
 *
 * @author zhangyancheng
 */
public class SyncDelivery extends BaseDelivery {

    public SyncDelivery() {
    }

    public SyncDelivery(DeliverExceptionHandler deliverExceptionHandler) {
        setDeliverExceptionHandler(deliverExceptionHandler);
    }

    @Override
    public Executor getDeliverTaskExecutor() {
        return deliverTaskExecutor;
    }

    private final Executor deliverTaskExecutor = new Executor() {
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    };
}
