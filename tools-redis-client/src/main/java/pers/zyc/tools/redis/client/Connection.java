package pers.zyc.tools.redis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.event.EventSource;
import pers.zyc.tools.event.Multicaster;
import pers.zyc.tools.utils.Stateful;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author zhangyancheng
 */
class Connection implements Stateful<ConnectionState>, Closeable, EventSource<ConnectionEvent>, ResponseListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);

	private final SocketNIO socketNio;

	private final Multicaster<EventListener<ConnectionEvent>> multicaster =
			new Multicaster<EventListener<ConnectionEvent>>() {};

	private Object response;
	private boolean responded;

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
		LOGGER.debug("Connection closed.");
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

	@Override
	public void onResponseReceived(Object response) {
		LOGGER.debug("Response received.");
		response(response);
		state(ConnectionState.WORKING);
	}

	@Override
	public void onSocketException(Exception e) {
		LOGGER.debug("Exception caught!", e);
		response(e);
		state(ConnectionState.EXCEPTION);
	}

	private void state(ConnectionState state) {
		this.state = state;
		multicaster.listeners.onEvent(new ConnectionEvent(this));
	}

	boolean isConnected() {
		return socketNio.channel.isConnected();
	}

	void sendRequest(Request request) {
		socketNio.request(request);
		responded = false;
	}

	Object getResponse(long timeout) {
		await(timeout);

		if (!responded) {
			LOGGER.debug("Request timeout!");
			state(ConnectionState.TIMEOUT);
			throw new RedisClientException("Request timeout");
		}

		if (response instanceof Throwable) {
			throw new RedisClientException((Throwable) response);
		}

		return response;
	}

	private synchronized void await(long timeout) {
		while (!responded && timeout > 0) {
			long now = System.currentTimeMillis();
			try {
				wait(timeout);
				timeout -= System.currentTimeMillis() - now;
			} catch (InterruptedException interrupted) {
				LOGGER.debug("Thread Interrupted!");
				Thread.currentThread().interrupt();
			}
		}
	}

	private synchronized void response(Object response) {
		this.response = response;
		this.responded = true;
		notify();
	}
}
