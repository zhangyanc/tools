package pers.zyc.tools.zkclient.listener;

import pers.zyc.tools.event.Listener;

/**
 * @author zhangyancheng
 */
public interface ConnectionListener extends Listener {
    /**
     * 连接成功, 新的ZooKeeper实例连接成功
     */
    void onConnected();

    /**
     * 重连成功(由suspend状态恢复)
     */
    void onReconnected();

    /**
     * 断线, 相当于Disconnected
     */
    void onSuspend();

    /**
     * 会话超时被关闭
     */
    void onSessionClosed();
}
