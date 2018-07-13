package pers.zyc.tools.utils.event;

/**
 * 事件监听器
 * @param <E> 事件泛型
 * @see EventSource
 * @author zhangyancheng
 */
public interface EventListener<E> extends Listener {

    void onEvent(E event);
}
