package pers.zyc.tools.redis.client;

import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.event.EventSource;
import pers.zyc.tools.event.Multicaster;
import pers.zyc.tools.redis.client.NetWorker.SocketNIO;
import pers.zyc.tools.redis.client.request.Request;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author zhangyancheng
 */
class Connection implements Closeable, EventSource<ConnectionEvent> {

	private final SocketNIO socketNio;

	private final Multicaster<EventListener<ConnectionEvent>> multicaster =
			new Multicaster<EventListener<ConnectionEvent>>() {};

	private Object response;

	private State state;

	Connection(SocketNIO socketNio) {
		this.socketNio = socketNio;
	}

	@Override
	public void close() {
		try {
			socketNio.close();
		} catch (IOException ignored) {
		}
	}

	@Override
	public void addListener(EventListener<ConnectionEvent> listener) {
		multicaster.addListener(listener);
	}

	@Override
	public void removeListener(EventListener<ConnectionEvent> listener) {
		multicaster.removeListener(listener);
	}

	boolean isBroken() {
		return state == State.REQUEST_TIMEOUT || state == State.BROKEN;
	}

	boolean canRecycle() {
		return state == State.REQUEST_TIMEOUT || state == State.BROKEN || state == State.RESPONSE_COMPLETED;
	}

	boolean isConnected() {
		return socketNio.channel().isConnected();
	}

	<R> ResponseFuture<R> sendRequest(Request request) {
		byte[] data = Protocol.encode(request);

		socketNio.request(data);
		changeState(State.REQUEST_SEND);

		return new ResponseFuture<>(this);
	}

	Object getResponse(long timeout) {
		synchronized (this) {
			while (response == null && timeout > 0) {
				long now = System.currentTimeMillis();
				try {
					wait(timeout);
					timeout -= System.currentTimeMillis() - now;
				} catch (InterruptedException interrupted) {
					Thread.currentThread().interrupt();
				}
			}
		}

		if (response == null) {
			changeState(State.REQUEST_TIMEOUT);
			throw new RedisClientException("Request timeout");
		}

		if (response instanceof Throwable) {
			throw new RedisClientException((Throwable) response);
		}
		return response;
	}

	void writeRequest() {
		try {
			socketNio.write();
			changeState(State.REQUEST_WROTE);
		} catch (Exception e) {
			response = e;

			synchronized (this) {
				notify();
			}
			changeState(State.BROKEN);
		}
	}

	void readResponse() {
		State newState;
		try {
			response = socketNio.read();
			newState = State.RESPONSE_COMPLETED;
		} catch (Exception e) {
			response = e;
			newState = State.BROKEN;
		} finally {
			synchronized (this) {
				notify();
			}
		}

		changeState(newState);
	}

	enum State {
		REQUEST_SEND,
		REQUEST_WROTE,
		REQUEST_TIMEOUT,
		RESPONSE_COMPLETED,
		BROKEN
	}

	private void changeState(State state) {
		this.state = state;
		multicaster.listeners.onEvent(new ConnectionEvent(this));
	}
}
