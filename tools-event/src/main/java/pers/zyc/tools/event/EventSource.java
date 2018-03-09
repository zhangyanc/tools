package pers.zyc.tools.event;

/**
 * 事件源
 * @param <E> 事件泛型
 * @see EventListener
 * @author zhangyancheng
 */
public interface EventSource<E> extends Listenable<EventListener<E>> {

    /**
     * 添加事件监听器
     * @param listener 事件监听器
     */
    @Override
    void addListener(EventListener<E> listener);

    /**
     * 删除事件监听器
     * @param listener 事件监听器
     */
    @Override
    void removeListener(EventListener<E> listener);
}
