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
import pers.zyc.tools.redis.client.request.Ping;
import pers.zyc.tools.redis.client.request.Quit;
import pers.zyc.tools.redis.client.request.Select;
import pers.zyc.tools.redis.client.util.Promise;
import pers.zyc.tools.utils.TimeMillis;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static pers.zyc.tools.redis.client.ResponseCast.STRING;

/**
 * @author zhangyancheng
 */
class ConnectionPool implements Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);

	private final String host, password;
	private final int port, db, connectionTimeout, requestTimeout;

	private final NetWorkGroup netWorkGroup;
	private final GenericObjectPool<Connection> pool;

	private final Responder responder = new Responder();
	private final NetWorkerHelper netWorkerHelper = new NetWorkerHelper();
	private final ConnectionRetriever connectionRetriever = new ConnectionRetriever();

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

	private class ConnectionRetriever implements EventListener<ConnectionEvent> {

		@Override
		public void onEvent(ConnectionEvent event) {
			LOGGER.debug("Retriever: {}", event);

			Connection connection = event.getSource();
			if (!connection.pooled) {
				return;
			}

			try {
				switch (event.eventType) {
					case REQUEST_TIMEOUT:
					case EXCEPTION_CAUGHT:
						pool.invalidateObject(connection);
						break;
					case RESPONSE_RECEIVED:
						pool.returnObject(connection);
						break;
				}
			} catch (Exception e) {
				LOGGER.error("Connection recycle error", e);
			}
		}
	}

	private class Responder implements EventListener<ConnectionEvent> {

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
	}

	private class NetWorkerHelper implements EventListener<ConnectionEvent> {

		@Override
		public void onEvent(ConnectionEvent event) {
			LOGGER.debug("Helper: {}", event);

			Connection connection = event.getSource();
			NetWorker netWorker = netWorkGroup.getNetWorker(connection.id);

			switch (event.eventType) {
				case REQUEST_SET:
					netWorker.enableWrite(connection);
					break;
				case REQUEST_SEND:
					netWorker.disableWrite(connection);
					break;
				case RESPONSE_RECEIVED:
				case CONNECTION_CLOSED:
				case EXCEPTION_CAUGHT:
					netWorker.finishRequest(connection);
					break;
			}
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

	private class ConnectionFactory implements PooledObjectFactory<Connection> {

		private final AtomicInteger connectionIds = new AtomicInteger();

		@Override
		public PooledObject<Connection> makeObject() throws Exception {
			SocketChannel channel = createChannel(host, port, connectionTimeout);

			int connId = connectionIds.getAndIncrement();
			Connection connection = new Connection(connId, channel, requestTimeout);

			netWorkGroup.getNetWorker(connId).register(connection);

			connection.addListener(responder);
			connection.addListener(netWorkerHelper);
			connection.addListener(connectionRetriever);

			if (password != null && !password.isEmpty()) {
				connection.send(new Auth(password), STRING).get();
			}
			if (db > 0) {
				connection.send(new Select(db), STRING).get();
			}

			connection.pooled = true;
			return new DefaultPooledObject<>(connection);
		}

		@Override
		public void destroyObject(PooledObject<Connection> p) throws Exception {
			Connection connection = p.getObject();
			connection.pooled = false;
			if (connection.channel.isConnected()) {
				try {
					connection.send(new Quit(), STRING).get();
				} catch (Exception ignored) {
				}
			}
			connection.close();
		}

		@Override
		public boolean validateObject(PooledObject<Connection> p) {
			Connection connection = p.getObject();
			if (connection.channel.isConnected()) {
				long ping = TimeMillis.get();
				boolean valid = connection.send(new Ping(), STRING).get().equals("PONG");
				LOGGER.debug("PING-PONG expend {} ms, {} validate {}", TimeMillis.get() - ping, connection, valid);
				return valid;
			}
			return false;
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
