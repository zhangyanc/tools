package pers.zyc.tools.redis.client;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.SwallowedExceptionListener;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.redis.client.exception.RedisClientException;
import pers.zyc.tools.redis.client.request.Auth;
import pers.zyc.tools.redis.client.request.Select;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import static pers.zyc.tools.redis.client.ResponseCast.STRING;

/**
 * @author zhangyancheng
 */
class ConnectionPool implements EventListener<ConnectionEvent>, Closeable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);

	private final String host, password;
	private final int port, db, connectionTimeout, requestTimeout;

	private final NetWorkGroup netWorkGroup;
	private final GenericObjectPool<Connection> pool;
	private final Responder responder = new Responder();

	ConnectionPool(ClientConfig config) {
		this.host = config.getHost();
		this.password = config.getPassword();
		this.port = config.getPort();
		this.db = config.getDb();
		this.connectionTimeout = config.getConnectionTimeout();
		this.requestTimeout = config.getRequestTimeout();

		netWorkGroup = new NetWorkGroup(config.getNetWorkers());
		pool = new GenericObjectPool<>(new ConnectionFactory(), config.getPoolConfig());
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
		netWorkGroup.close();
	}

	Connection getConnection() {
		try {
			return pool.borrowObject();
		} catch (Exception e) {
			throw new RedisClientException("Could not get a connection from the pool", e);
		}
	}

	private Connection createConnection(SocketChannel channel) throws IOException {
		Connection connection = new Connection(channel, responder, requestTimeout);

		netWorkGroup.register(connection);

		if (password != null && !password.isEmpty()) {
			connection.send(new Auth(password), STRING).get();
		}
		if (db > 0) {
			connection.send(new Select(db), STRING).get();
		}

		connection.addListener(this);
		return connection;
	}

	private class ConnectionFactory implements PooledObjectFactory<Connection> {

		@Override
		public PooledObject<Connection> makeObject() throws Exception {
			SocketChannel channel = createChannel(host, port, connectionTimeout);
			return new DefaultPooledObject<>(createConnection(channel));
		}

		@Override
		public void destroyObject(PooledObject<Connection> p) throws Exception {
			Connection connection = p.getObject();
			if (connection.isConnected()) {
				//TODO quit and close
				connection.close();
			}
			connection.close();
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

	private static SocketChannel createChannel(String host, int port, int connectionTimeout) throws IOException {
		SocketChannel channel = SocketChannel.open();
		try {
			channel.socket().setSoLinger(false, -1);
			channel.socket().setTcpNoDelay(true);
			SocketAddress connectTo = new InetSocketAddress(host, port);
			if (connectionTimeout > 0) {
				channel.socket().connect(connectTo, connectionTimeout);
			} else {
				channel.connect(connectTo);
			}
			channel.configureBlocking(false);
			return channel;
		} catch (IOException e) {
			try {
				channel.close();
			} catch (IOException ignored) {
			}
			throw e;
		}
	}
}
