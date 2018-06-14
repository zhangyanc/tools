package pers.zyc.tools.redis.client;

import pers.zyc.tools.event.SourcedEvent;

/**
 * @author zhangyancheng
 */
class ConnectionEvent extends SourcedEvent<Connection> {
	final EventType eventType;

	ConnectionEvent(Connection source, EventType eventType) {
		super(source);
		this.eventType = eventType;
	}

	enum EventType {
		REQUEST_SET,
		REQUEST_SEND,
		REQUEST_TIMEOUT,
		RESPONSE_RECEIVED,
		EXCEPTION_CAUGHT,
		CONNECTION_CLOSED;
	}

	@Override
	public String toString() {
		return "ConnectionEvent{" +
				"eventType=" + eventType +
				", connection=" + source + "}";
	}

	static class RequestSet extends ConnectionEvent {

		RequestSet(Connection source) {
			super(source, EventType.REQUEST_SET);
		}
	}

	static class RequestSend extends ConnectionEvent {

		RequestSend(Connection source) {
			super(source, EventType.REQUEST_SEND);
		}
	}

	static class RequestTimeout extends ConnectionEvent {

		RequestTimeout(Connection source) {
			super(source, EventType.REQUEST_TIMEOUT);
		}
	}

	static class ResponseReceived extends ConnectionEvent {

		ResponseReceived(Connection source) {
			super(source, EventType.RESPONSE_RECEIVED);
		}
	}

	static class ExceptionCaught extends ConnectionEvent {

		ExceptionCaught(Connection source) {
			super(source, EventType.EXCEPTION_CAUGHT);
		}
	}

	static class ConnectionClosed extends ConnectionEvent {

		ConnectionClosed(Connection source) {
			super(source, EventType.CONNECTION_CLOSED);
		}
	}
}
