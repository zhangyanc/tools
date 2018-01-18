package pers.zyc.tools.event;

/**
 * 监听事件源
 *
 * @author zhangyancheng
 */
public interface EventSource<E> extends Listenable<EventListener<E>> {

    /**
     * 设置listener调用器
     */
    void setListenerInvoker(ListenerInvoker listenerInvoker);
}
