package pers.zyc.tools.redis.client;

import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.redis.client.exception.RedisClientException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author zhangyancheng
 */
class Responder implements EventListener<ConnectionEvent> {
	private final ConcurrentMap<Connection, Promise<?>> respondingMap = new ConcurrentHashMap<>();

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

		final Promise promise = respondingMap.remove(event.getSource());
		if (promise != null) {
			promise.response(response);
		}
	}

	<R> Future<R> respond(final Connection connection, final ResponseCast<R> responseCast) {
		return new Promise<R>() {

			{
				respondingMap.put(connection, this);
			}

			final CountDownLatch responseLatch = new CountDownLatch(1);

			Object response;

			@Override
			void response(Object response) {
				this.response = response;
				responseLatch.countDown();
			}

			@Override
			public R get() {
				try {

					responseLatch.await();

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
