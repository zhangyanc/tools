package pers.zyc.tools.event;

import java.util.concurrent.Executor;

/**
 * 事件广播
 *
 * @author zhangyancheng
 * @see SyncEventMulticaster
 * @see AsyncEventMulticaster
 */
public interface EventMulticaster {

    /**
     * 添加监听器, 同一个listener不会重复添加
     * @param listener 监听器
     * @throws NullPointerException listener为null抛出
     */
    void addListener(EventListener listener);

    /**
     * 删除监听器
     * @param listener 监听器
     */
    void removeListener(EventListener listener);

    /**
     * 清空所有监听器
     */
    void removeAllListeners();

    /**
     * 向已添加的所有监听器广播事件
     * @param event 事件
     */
    void multicastEvent(Object event);

    /**
     * @return 广播任务执行器
     */
    Executor getMulticastExecutor();

    /**
     * 设置事件回调异常处理器
     * @param multicastExceptionHandler 事件回调异常处理器
     */
    void setDeliverExceptionHandler(MulticastExceptionHandler multicastExceptionHandler);
}
