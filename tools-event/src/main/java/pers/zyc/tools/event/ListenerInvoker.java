package pers.zyc.tools.event;

/**
 * @author zhangyancheng
 */
public interface ListenerInvoker {

    <E> void invoke(EventListener<E> listener, E event);
}
