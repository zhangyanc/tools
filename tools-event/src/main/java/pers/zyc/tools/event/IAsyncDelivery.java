package pers.zyc.tools.event;

import java.util.concurrent.ExecutorService;

/**
 * 并行发布器
 *
 * @author zhangyancheng
 */
public interface IAsyncDelivery extends EventDelivery {

    ExecutorService getDeliverTaskExecutor();
}
