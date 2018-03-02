package pers.zyc.tools.zkclient;

import org.apache.zookeeper.ZooKeeper;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.event.EventSource;
import pers.zyc.tools.zkclient.event.ConnectionEvent;

/**
 * ZooKeeper连接器, 自动重连及连接变更事件发布
 *
 * @author zhangyancheng
 */
public interface ZKConnector extends EventSource<ConnectionEvent> {

    /**
     * @return 当前是否成功连接ZooKeeper
     */
    boolean isConnected();

    /**
     * 返回ZooKeeper实例, 可能为null
     * @return ZooKeeper实例
     */
    ZooKeeper getZooKeeper();

    /**
     * 添加连接事件监听器, 添加后可持续收到连接事件
     *
     * <p>连接状态变更后发布连接事件, 按照listener的添加顺序依次发布(应避免回调中的阻塞操作)
     * <p>如果为SpecialEventListener
     *
     * @param listener 连接事件监听器, 同一个监听器只会添加一次
     * @throws NullPointerException listener为null抛出
     */
    @Override
    void addListener(EventListener<ConnectionEvent> listener);

    /**
     * 移除连接事件监听器, 移除后不再收到连接事件
     *
     * @param listener 连接事件监听器
     */
    @Override
    void removeListener(EventListener<ConnectionEvent> listener);
}
