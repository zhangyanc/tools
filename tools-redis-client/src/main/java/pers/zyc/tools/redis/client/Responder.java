package pers.zyc.tools.redis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.redis.client.exception.RedisClientException;
import pers.zyc.tools.redis.client.util.Promise;

import java.io.Closeable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhangyancheng
 */
public class Responder implements EventListener<ConnectionEvent>, Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger(Responder.class);

	private final ConcurrentMap<Connection, Promise<?>> respondingMap = new ConcurrentHashMap<>();

	@Override
	public void onEvent(ConnectionEvent event) {
		LOGGER.debug("Responder: {}", event);

		final Object response;
		switch (event.eventType) {
			case REQUEST_SET:
				Promise<?> promise = (Promise<?>) event.payload();
				respondingMap.put(event.getSource(), promise);
				return;
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

	@Override
	public void close() {
		for (Connection connection : respondingMap.keySet()) {
			connection.close();
		}
	}
}
