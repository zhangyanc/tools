package pers.zyc.tools.zkclient.event;

/**
 * @author zhangyancheng
 */
public enum ConnectionEvent {
    /**
     * 连接成功(新会话)
     */
    CONNECTED,
    /**
     * 断开连接
     */
    DISCONNECTED,
    /**
     * 超时前重连成功(老会话)
     */
    RECONNECTED,
    /**
     * 会话关闭
     */
    SESSION_CLOSED;
}
