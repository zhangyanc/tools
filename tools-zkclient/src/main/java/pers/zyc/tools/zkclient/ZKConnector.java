package pers.zyc.tools.zkclient;

import org.apache.zookeeper.ZooKeeper;
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
}
