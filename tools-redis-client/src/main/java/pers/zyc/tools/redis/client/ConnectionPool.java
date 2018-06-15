package pers.zyc.tools.redis.client;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.SwallowedExceptionListener;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.lifecycle.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhangyancheng
 */
public class ConnectionPool extends Service {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);
	private static final int DEFAULT_REQUEST_TIMEOUT = 60000;
	private static final int DEFAULT_NET_WORKERS = 1;

	private final URI uri;
	private final GenericObjectPoolConfig poolConfig;
	private final NetWorker[] netWorkers;
	private final AtomicInteger chooseIndexer = new AtomicInteger();

	private GenericObjectPool<Connection> internalPool;

	public ConnectionPool(String connectStr) throws Exception {
		this(connectStr, new GenericObjectPoolConfig());
	}

	public ConnectionPool(String connectStr,
						  GenericObjectPoolConfig poolConfig) throws Exception {
		this(connectStr, poolConfig, DEFAULT_REQUEST_TIMEOUT);
	}

	public ConnectionPool(String connectStr,
						  GenericObjectPoolConfig poolConfig,
						  long requestTimeout) throws Exception {

		this.uri = URI.create(connectStr);
		this.poolConfig = Objects.requireNonNull(poolConfig);
		this.netWorkers = new NetWorker[DEFAULT_NET_WORKERS];

		if (requestTimeout <= 0) {
			requestTimeout = DEFAULT_REQUEST_TIMEOUT;
		}
		for (int i = 0; i < DEFAULT_NET_WORKERS; i++) {
			netWorkers[i] = new NetWorker(requestTimeout);
		}
	}

	@Override
	protected void doStart() {
		for (NetWorker netWorker : netWorkers) {
			netWorker.start();
		}
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
		internalPool.close();
		for (NetWorker netWorker : netWorkers) {
			netWorker.stop();
		}
	}

	Connection getConnection() {
		if (!isRunning()) {
			throw new RedisClientException("Connection is not running!");
		}

		try {
			return internalPool.borrowObject();
		} catch (Exception e) {
			throw new RedisClientException("Could not get a connection from the pool", e);
		}
	}

	private NetWorker chooseNetWorker() {
		return netWorkers[chooseIndexer.getAndIncrement() % netWorkers.length];
	}

	private class ConnectionFactory implements EventListener<ConnectionEvent>, PooledObjectFactory<Connection> {

		@Override
		public void onEvent(ConnectionEvent event) {
			Connection connection = event.getSource();
			try {
				switch (event.eventType) {
					case REQUEST_TIMEOUT:
					case EXCEPTION_CAUGHT:
						internalPool.invalidateObject(connection);
						break;
					case RESPONSE_RECEIVED:
						internalPool.returnObject(connection);
						break;
					default:
						LOGGER.debug("OnEvent: {}", event);
				}
			} catch (Exception e) {
				LOGGER.error("Connection recycle error", e);
			}
		}

		@Override
		public PooledObject<Connection> makeObject() throws Exception {
			SocketChannel channel = createChannel(uri.getHost(), uri.getPort());
			Connection connection = new Connection(channel);
			connection.addListener(this);
			chooseNetWorker().register(connection);
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

	private static SocketChannel createChannel(String host, int port) throws IOException {
		SocketChannel sock = SocketChannel.open();
		try {
			sock.socket().setSoLinger(false, -1);
			sock.socket().setTcpNoDelay(true);
			sock.connect(new InetSocketAddress(host, port));
			sock.configureBlocking(false);
			return sock;
		} catch (IOException e) {
			try {
				sock.close();
			} catch (IOException ignored) {
			}
			throw e;
		}
	}
}
