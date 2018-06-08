package pers.zyc.tools.redis.client;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.SwallowedExceptionListener;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.lifecycle.Service;

import java.net.URI;

/**
 * @author zhangyancheng
 */
public class ConnectionPool extends Service {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);

	private final URI uri;
	private final GenericObjectPoolConfig poolConfig;
	private final NetWorker netWorker = new NetWorker();

	private GenericObjectPool<Connection> internalPool;
	private long requestTimeout;

	public ConnectionPool(String connectStr,
						  GenericObjectPoolConfig poolConfig,
						  long requestTimeout) throws Exception {

		this.uri = URI.create(connectStr);
		this.poolConfig = poolConfig;
		this.requestTimeout = requestTimeout;
	}

	@Override
	protected void doStart() {
		netWorker.start();

		internalPool = new GenericObjectPool<>(new ConnectionFactory(), poolConfig);
		internalPool.setSwallowedExceptionListener(new SwallowedExceptionListener() {
			@Override
			public void onSwallowException(Exception e) {
				LOGGER.error("Pool exception caught", e);
			}
		});
	}

	@Override
	protected void doStop() throws Exception {
		netWorker.stop();
		internalPool.close();
	}

	long getRequestTimeout() {
		return requestTimeout;
	}

	Connection getConnection() {
		try {
			return internalPool.borrowObject();
		} catch (Exception e) {
			throw new RedisClientException("Could not get a connection from the pool", e);
		}
	}

	private void recycle(Connection connection) {
		try {
			if (connection.getState() == ConnectionState.WORKING) {
				internalPool.returnObject(connection);
			} else {
				internalPool.invalidateObject(connection);
			}
		} catch (Exception e) {
			LOGGER.error("Connection recycle error", e);
		}
	}

	private class ConnectionFactory implements ConnectionListener, PooledObjectFactory<Connection> {

		@Override
		public void onEvent(ConnectionEvent event) {
			recycle(event.getSource());
		}

		@Override
		public PooledObject<Connection> makeObject() throws Exception {
			SocketNIO socket = netWorker.createSocket(uri.getHost(), uri.getPort());

			Connection connection = new Connection(socket);
			connection.addListener(this);
			socket.addListener(connection);

			return new DefaultPooledObject<>(connection);
		}

		@Override
		public void destroyObject(PooledObject<Connection> p) throws Exception {
			Connection connection = p.getObject();
			if (connection.isConnected()) {
				//TODO quit and close
				connection.close();
			}
		}

		@Override
		public boolean validateObject(PooledObject<Connection> p) {
			//TODO PING-PONG
			return p.getObject().isConnected();
		}

		@Override
		public void activateObject(PooledObject<Connection> p) throws Exception {

		}

		@Override
		public void passivateObject(PooledObject<Connection> p) throws Exception {

		}
	}
}
