package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.util.Promise;
import pers.zyc.tools.utils.event.SourcedEvent;

/**
 * @author zhangyancheng
 */
class ConnectionEvent extends SourcedEvent<Connection> {
	final EventType eventType;

	private Object payload;

	ConnectionEvent(Connection source, EventType eventType) {
		super(source);
		this.eventType = eventType;
	}

	Object payload() {
		return payload;
	}

	void payload(Object payload) {
		this.payload = payload;
	}

	enum EventType {
		REQUEST_SET,
		REQUEST_SEND,
		REQUEST_TIMEOUT,
		RESPONSE_RECEIVED,
		EXCEPTION_CAUGHT,
		CONNECTION_CLOSED
	}

	@Override
	public String toString() {
		return "ConnectionEvent{" +
				"eventType=" + eventType +
				", connection=" + source + "}";
	}

	static class RequestSet extends ConnectionEvent {

		RequestSet(Connection source, Promise<?> promise) {
			super(source, EventType.REQUEST_SET);
			payload(promise);
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

		ResponseReceived(Connection source, Object response) {
			super(source, EventType.RESPONSE_RECEIVED);
			payload(response);
		}
	}

	static class ExceptionCaught extends ConnectionEvent {

		ExceptionCaught(Connection source, Exception exception) {
			super(source, EventType.EXCEPTION_CAUGHT);
			payload(exception);
		}
	}

	static class ConnectionClosed extends ConnectionEvent {

		ConnectionClosed(Connection source) {
			super(source, EventType.CONNECTION_CLOSED);
		}
	}
}
