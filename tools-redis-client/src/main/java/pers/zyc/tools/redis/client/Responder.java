package pers.zyc.tools.redis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.redis.client.exception.RedisClientException;
import pers.zyc.tools.redis.client.util.Promise;
import pers.zyc.tools.utils.Pair;
import pers.zyc.tools.utils.TimeMillis;
import pers.zyc.tools.utils.event.EventListener;
import pers.zyc.tools.utils.lifecycle.ThreadService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhangyancheng
 */
class Responder extends ThreadService implements EventListener<ConnectionEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Responder.class);

	private final int requestTimeout;
	private final int requestTimeoutDetectInterval;
	private final ConcurrentMap<Connection, PromiseInfo> respondingMap = new ConcurrentHashMap<>();

	Responder(ClientConfig clientConfig) {
		this.requestTimeout = clientConfig.getRequestTimeout();
		this.requestTimeoutDetectInterval = clientConfig.getRequestTimeoutDetectInterval();
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();

		for (Connection connection : respondingMap.keySet()) {
			connection.close();
		}
	}

	@Override
	protected Runnable getRunnable() {
		return new ServiceRunnable() {

			@Override
			protected long getInterval() {
				return requestTimeoutDetectInterval;
			}

			@Override
			protected void execute() throws InterruptedException {
				for (Map.Entry<Connection, PromiseInfo> entry : respondingMap.entrySet()) {
					if (entry.getValue().isTimeout()) {
						entry.getKey().timeout();
					}
				}
			}
		};
	}

	@Override
	public void onEvent(ConnectionEvent event) {
		LOGGER.debug("Responder: {}", event);

		final Object response;
		switch (event.eventType) {
			case REQUEST_SET:
				long timeoutLine = TimeMillis.INSTANCE.get() + requestTimeout;
				respondingMap.put(event.getSource(), new PromiseInfo((Promise<?>) event.payload(), timeoutLine));
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
			case REQUEST_SEND:
			default:
				return;
		}

		final PromiseInfo promiseInfo = respondingMap.remove(event.getSource());
		if (promiseInfo != null) {
			promiseInfo.key().response(response);
		}
	}

	private static class PromiseInfo extends Pair<Promise<?>, Long> {

		PromiseInfo(Promise<?> promise, long timeoutLine) {
			key(promise);
			value(timeoutLine);
		}

		boolean isTimeout() {
			return value() <= TimeMillis.INSTANCE.get();
		}
	}
}
