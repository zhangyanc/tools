package pers.zyc.tools.redis.client;

import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.event.EventSource;
import pers.zyc.tools.event.Multicaster;
import pers.zyc.tools.redis.client.NetWorker.SocketNIO;
import pers.zyc.tools.redis.client.request.Request;
import pers.zyc.tools.utils.Stateful;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author zhangyancheng
 */
class Connection implements Stateful<ConnectionState>, Closeable, EventSource<ConnectionEvent> {

	private final SocketNIO socketNio;

	private final Multicaster<EventListener<ConnectionEvent>> multicaster =
			new Multicaster<EventListener<ConnectionEvent>>() {};

	private Object response;

	private ConnectionState state = ConnectionState.WORKING;

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

	@Override
	public ConnectionState getState() {
		return state;
	}

	@Override
	public boolean checkState(ConnectionState state) {
		return this.state == state;
	}

	private void state(ConnectionState state) {
		this.state = state;
		multicaster.listeners.onEvent(new ConnectionEvent(this));
	}

	boolean isConnected() {
		return socketNio.channel().isConnected();
	}

	<R> ResponseFuture<R> sendRequest(Request request) {
		byte[] data = Protocol.encode(request);

		socketNio.request(data);

		return new ResponseFuture<>(this);
	}

	Object getResponse(long timeout) {
		await(timeout);

		if (response == null) {
			state(ConnectionState.TIMEOUT);
			throw new RedisClientException("Request timeout");
		}

		if (response instanceof Throwable) {
			throw new RedisClientException((Throwable) response);
		}
		return response;
	}

	private synchronized void await(long timeout) {
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

	private synchronized void response(Object response) {
		this.response = response;
		notify();
	}

	void writeRequest() {
		try {
			socketNio.write();
		} catch (Exception e) {
			response(e);
			state(ConnectionState.EXCEPTION);
		}
	}

	void readResponse() {
		try {
			response(socketNio.read());
		} catch (Exception e) {
			response(e);
			state(ConnectionState.EXCEPTION);
		}
	}
}
