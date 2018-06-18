package pers.zyc.tools.redis.client;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.SwallowedExceptionListener;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventListener;

import java.io.Closeable;

/**
 * @author zhangyancheng
 */
class ConnectionPool implements EventListener<ConnectionEvent>, Closeable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);

	private final GenericObjectPool<Connection> pool;

	ConnectionPool(PooledObjectFactory<Connection> connectionFactory, GenericObjectPoolConfig poolConfig) throws Exception {
		pool = new GenericObjectPool<>(connectionFactory, poolConfig);
		pool.setSwallowedExceptionListener(new SwallowedExceptionListener() {
			@Override
			public void onSwallowException(Exception e) {
				LOGGER.error("Pool exception caught", e);
			}
		});
	}

	@Override
	public void onEvent(ConnectionEvent event) {
		Connection connection = event.getSource();
		try {
			switch (event.eventType) {
				case REQUEST_TIMEOUT:
				case EXCEPTION_CAUGHT:
					pool.invalidateObject(connection);
					break;
				case RESPONSE_RECEIVED:
					pool.returnObject(connection);
					break;
				default:
					LOGGER.debug("OnEvent: {}", event);
			}
		} catch (Exception e) {
			LOGGER.error("Connection recycle error", e);
		}
	}

	@Override
	public void close() {
		pool.close();
	}

	Connection getConnection() {
		try {
			return pool.borrowObject();
		} catch (Exception e) {
			throw new RedisClientException("Could not get a connection from the pool", e);
		}
	}
}
