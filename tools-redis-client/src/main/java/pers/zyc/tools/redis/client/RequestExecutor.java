package pers.zyc.tools.redis.client;

import pers.zyc.tools.event.EventListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author zhangyancheng
 */
class RequestExecutor implements EventListener<ConnectionEvent> {

	private final ConnectionPool connectionPool;
	private final ConcurrentMap<Connection, Responder> responderMap = new ConcurrentHashMap<>();

	RequestExecutor(ConnectionPool connectionPool) {
		this.connectionPool = connectionPool;
	}

	@Override
	public void onEvent(ConnectionEvent event) {
		final Object response;
		switch (event.eventType) {
			case CONNECTION_CLOSED:
				response = new RedisClientException("Connection closed!");
				break;
			case REQUEST_TIMEOUT:
				response = new RedisClientException("Request timeout!");
				break;
			case EXCEPTION_CAUGHT:
			case RESPONSE_RECEIVED:
				response = event.payload();
				break;
			default:
				return;
		}

		final Responder responder = responderMap.remove(event.getSource());
		if (responder != null) {
			responder.respond(response);
		}
	}

	private static class Responder extends CountDownLatch {

		private Object response;

		Responder() {
			super(1);
		}

		void respond(Object response) {
			this.response = response;
			countDown();
		}

		Object ask() throws InterruptedException {
			await();
			return response;
		}
	}

	<R> ResponseFuture<R> execute(Request request, final ResponseCast<R> responseCast) {
		final Responder responder = new Responder();
		final Connection connection = connectionPool.getConnection();

		responderMap.put(connection, responder);
		connection.sendRequest(request);

		return new ResponseFuture<R>() {

			@Override
			public R get() {
				try {
					Object response = responder.ask();

					if (response instanceof Throwable) {
						if (response instanceof RedisClientException) {
							throw (RedisClientException) response;
						}
						throw new RedisClientException((Throwable) response);
					}

					return responseCast.cast(response);
				} catch (InterruptedException e) {
					throw new RedisClientException("Thread Interrupted");
				}
			}
		};
	}
}
