package pers.zyc.tools.event;

import java.util.Collection;
import java.util.concurrent.Executor;

/**
 * 事件递送器
 *
 * @author zhangyancheng
 * @see SyncDelivery
 * @see AsyncDelivery
 */
public interface EventDelivery {

    <E extends Event> void deliver(E event, EventListener<E> eventListener);

    <E extends Event> void deliver(E event, Collection<EventListener<E>> eventListeners);

    Executor getDeliverTaskExecutor();

    void setDeliverExceptionHandler(DeliverExceptionHandler deliverExceptionHandler);
}
