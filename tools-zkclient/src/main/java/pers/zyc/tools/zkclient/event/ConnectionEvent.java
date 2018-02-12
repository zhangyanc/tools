package pers.zyc.tools.zkclient.event;

import pers.zyc.tools.event.Event;
import pers.zyc.tools.zkclient.ZKConnector;

/**
 * @author zhangyancheng
 */
public class ConnectionEvent extends Event<ZKConnector> {

    public enum EventType {
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

    private EventType eventType;

    public ConnectionEvent(ZKConnector source, EventType eventType) {
        super(source);
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }
}
