package pers.zyc.tools.redis.client;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.SwallowedExceptionListener;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.redis.client.exception.RedisClientException;
import pers.zyc.tools.redis.client.request.Auth;
import pers.zyc.tools.redis.client.request.Ping;
import pers.zyc.tools.redis.client.request.Quit;
import pers.zyc.tools.redis.client.request.Select;
import pers.zyc.tools.redis.client.util.Promise;
import pers.zyc.tools.utils.Pair;
import pers.zyc.tools.utils.TimeMillis;
import pers.zyc.tools.utils.event.EventListener;
import pers.zyc.tools.utils.lifecycle.ThreadService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import static pers.zyc.tools.redis.client.ResponseCast.STRING;

/**
 * @author zhangyancheng
 */
class ConnectionPool extends ThreadService implements EventListener<ConnectionEvent>, PooledObjectFactory<Connection> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);

	private final ClientConfig config;
	private final NetWorkGroup netWorkGroup;
	private final GenericObjectPool<Connection> pool;
	private final Condition stopCondition = serviceLock.newCondition();
	private final ConcurrentMap<Connection, PromiseInfo> respondingMap = new ConcurrentHashMap<>();

	ConnectionPool(ClientConfig config) {
		this.config = config;
		netWorkGroup = new NetWorkGroup(config.getNetWorkers());

		pool = new GenericObjectPool<>(this, config.createPoolConfig());
		start();
	}

	@Override
	protected void doStart() {
		netWorkGroup.start();

		if (config.isNeedPreparePool()) {
			try {
				pool.preparePool();
			} catch (Exception e) {
				throw new RedisClientException("Pool prepare error", e);
			}
		}
		pool.setSwallowedExceptionListener(new SwallowedExceptionListener() {
			@Override
			public void onSwallowException(Exception e) {
				LOGGER.error("Pool exception swallowed", e);
			}
		});
	}

	@Override
	protected void doStop() throws Exception {
		pool.close();
		while (pool.getNumActive() > 0) {
			stopCondition.await(1000, TimeUnit.MILLISECONDS);
		}
		netWorkGroup.stop();
		super.doStop();
	}

	@Override
	protected Runnable getRunnable() {
		return new ServiceRunnable() {

			@Override
			protected long getInterval() {
				return config.getRequestTimeoutDetectInterval();
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

	Connection getConnection() {
		try {
			return pool.borrowObject();
		} catch (Exception e) {
			throw new RedisClientException("Could not get a connection from the pool", e);
		}
	}

	@Override
	public void onEvent(ConnectionEvent event) {
		final Connection connection = event.getSource();
		final Object response;

		switch (event.eventType) {
			case REQUEST_SET:
				long timeoutLine = TimeMillis.INSTANCE.get() + config.getRequestTimeout();
				respondingMap.put(connection, new PromiseInfo((Promise<?>) event.payload(), timeoutLine));
				return;
			case REQUEST_SEND:
			case CONNECTION_CLOSED:
				return;
			case EXCEPTION_CAUGHT:
			case RESPONSE_RECEIVED:
			case REQUEST_TIMEOUT:
				response = event.payload();
				break;
			default:
				throw new Error();
		}

		respondingMap.remove(connection).key().response(response);

		if (connection.isActivated()) {
			if (response instanceof Exception) {
				try {
					pool.invalidateObject(connection);
				} catch (Exception e) {
					LOGGER.error("Invalidate connection exception!", e);
				}
			} else {
				pool.returnObject(connection);
			}
		}
	}

	@Override
	public PooledObject<Connection> makeObject() throws Exception {
		if (netWorkGroup.inNetworking()) {
			throw new RedisClientException("Can't create connection in networking!");
		}
		SocketChannel channel = createChannel(config.getHost(), config.getPort(), config.getConnectionTimeout());
		Connection connection = new Connection(channel, netWorkGroup.next());
		try {
			connection.addListener(this);

			if (config.getPassword() != null) {
				connection.send(new Auth(config.getPassword()), STRING).get();
			}
			if (config.getDb() > 0) {
				connection.send(new Select(config.getDb()), STRING).get();
			}
			connection.setActivated(true);
			LOGGER.info("Created new {}", connection);
			return new DefaultPooledObject<>(connection);
		} catch (Exception e) {
			connection.close();
			throw e;
		}
	}

	@Override
	public void destroyObject(PooledObject<Connection> p) throws Exception {
		try (Connection connection = p.getObject()) {
			connection.setActivated(false);
			if (!netWorkGroup.inNetworking() && connection.isConnected()) {
				connection.send(new Quit(), STRING).get();
			}
		}
	}

	@Override
	public boolean validateObject(PooledObject<Connection> p) {
		Connection connection = p.getObject();
		if (connection.isConnected()) {
			long ping = TimeMillis.INSTANCE.get();
			boolean valid = connection.send(new Ping(), STRING).get().equals("PONG");
			LOGGER.debug("PING-PONG expend {} ms, {} validate {}", TimeMillis.INSTANCE.get() - ping, connection, valid);
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
