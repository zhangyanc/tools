package pers.zyc.tools.event;

import pers.zyc.tools.utils.TimeMillis;

import java.util.EventObject;

/**
 * @author zhangyancheng
 */
public class SourcedEvent<S extends EventSource> extends EventObject {
    private long eventTime;

    public SourcedEvent(S source) {
        super(source);
        this.eventTime = TimeMillis.get();
    }

    /**
     * 事件生成事件
     */
    public long getEventTime() {
        return eventTime;
    }

    @Override
    @SuppressWarnings("unchecked")
    public S getSource() {
        return (S) super.getSource();
    }
}