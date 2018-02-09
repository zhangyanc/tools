package pers.zyc.tools.event;

import java.util.Collection;

/**
 * 事件发布
 *
 * @author zhangyancheng
 */
public interface EventPublisher {

    /**
     * 对监听器发布事件
     *
     * @param event 事件
     * @param listener 监听器
     * @param <E> 事件类型
     */
    <E> void publish(E event, EventListener<E> listener);

    /**
     * 对集合所有监听器发布事件
     *
     * @param event 事件
     * @param listeners 监听器集合
     * @param <E> 事件类型
     */
    <E> void publish(E event, Collection<EventListener<E>> listeners);

    /**
     * @param publishExceptionHandler 发布异常处理器
     */
    void setPublishExceptionHandler(PublishExceptionHandler publishExceptionHandler);
}
