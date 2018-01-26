package pers.zyc.tools.event;

import java.util.Collection;

/**
 * @author zhangyancheng
 */
public interface ListenerInvoker {

    <E> void invoke(E event, EventListener<E> listener);

    <E> void invoke(E event, Collection<EventListener<E>> listeners);

    void setExceptionHandler(ExceptionHandler exceptionHandler);
}
