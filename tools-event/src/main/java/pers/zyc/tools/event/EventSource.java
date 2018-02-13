package pers.zyc.tools.event;

/**
 * 监听事件源
 *
 * @author zhangyancheng
 */
public interface EventSource<E extends Event> extends Listenable<EventListener<E>> {

    /**
     * 添加事件监听
     */
    void addListener(EventListener<E> listener);

    /**
     * 移除事件监听
     */
    void removeListener(EventListener<E> listener);
}
