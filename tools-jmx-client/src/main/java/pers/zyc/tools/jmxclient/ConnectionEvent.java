package pers.zyc.tools.jmxclient;

import pers.zyc.tools.utils.event.SourcedEvent;

/**
 * @author zhangyancheng
 */
public class ConnectionEvent extends SourcedEvent<JmxClient> {
	private final String eventType;

	ConnectionEvent(JmxClient source, String eventType) {
		super(source);
		this.eventType = eventType;
	}

	public String getEventType() {
		return eventType;
	}

	@Override
	public String toString() {
		return "ConnectionEvent{" +
				"eventType='" + eventType + '\'' +
				'}';
	}
}
