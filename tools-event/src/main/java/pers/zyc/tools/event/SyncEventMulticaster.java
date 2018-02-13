package pers.zyc.tools.event;

import java.util.concurrent.Executor;

/**
 * 在multicast调用线程执行事件回调
 * @author zhangyancheng
 */
public class SyncEventMulticaster extends AbstractEventMulticaster {

    public SyncEventMulticaster() {
    }

    public SyncEventMulticaster(MulticastExceptionHandler multicastExceptionHandler) {
        setDeliverExceptionHandler(multicastExceptionHandler);
    }

    @Override
    public Executor getMulticastExecutor() {
        return SYNC_EXECUTOR;
    }

    private static final Executor SYNC_EXECUTOR = new Executor() {
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    };
}
