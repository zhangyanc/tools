package pers.zyc.tools.redis.client;

import pers.zyc.tools.event.EventListener;

import java.util.Objects;

/**
 * @author zhangyancheng
 */
class RequestAction<R> implements EventListener<ConnectionEvent> {

	private Request request;
	private Connection connection;
	private ResponseCast<R> responseCast;

	private Object response;
	private boolean responded;

	RequestAction<R> request(Request request) {
		this.request = request;
		return this;
	}

	RequestAction<R> connection(Connection connection) {
		this.connection = connection;
		return this;
	}

	RequestAction<R> responseCast(ResponseCast<R> responseCast) {
		this.responseCast = responseCast;
		return this;
	}

	private void validate() {
		Objects.requireNonNull(request);
		Objects.requireNonNull(connection);
		Objects.requireNonNull(responseCast);
	}

	ResponseFuture<R> execute() {
		validate();

		connection.sendRequest(request);
		connection.addListener(this);

		return new ResponseFuture<R>() {

			@Override
			public R get() {
				await();

				if (response instanceof Throwable) {
					if (response instanceof RedisClientException) {
						throw (RedisClientException) response;
					}
					throw new RedisClientException((Throwable) response);
				}

				return responseCast.cast(response);
			}
		};
	}

	private synchronized void await() {
		while (!responded) {
			try {
				wait();
			} catch (InterruptedException interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private synchronized void respond(Object response) {
		this.response = response;
		this.responded = true;
		notify();
	}

	@Override
	public void onEvent(ConnectionEvent event) {
		if (event.eventType == ConnectionEvent.EventType.REQUEST_SEND) {
			return;
		}

		try {
			switch (event.eventType) {
				case CONNECTION_CLOSED:
					respond(new RedisClientException("Connection closed!"));
					break;
				case REQUEST_TIMEOUT:
					respond(new RedisClientException("Request timeout!"));
					break;
				case EXCEPTION_CAUGHT:
				case RESPONSE_RECEIVED:
					respond(event.payload());
					break;
			}
		} finally {
			connection.removeListener(this);
		}
	}
}
