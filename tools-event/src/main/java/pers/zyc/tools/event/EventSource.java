package pers.zyc.tools.event;

/**
 * 事件源
 *
 * @author zhangyancheng
 */
public interface EventSource<L extends EventListener> extends Listenable<L> {

    /**
     * 添加事件监听器
     * @param listener 事件监听器
     */
    @Override
    void addListener(L listener);

    /**
     * 删除事件监听器
     * @param listener 事件监听器
     */
    @Override
    void removeListener(L listener);
}
