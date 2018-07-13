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
import pers.zyc.tools.utils.TimeMillis;
import pers.zyc.tools.utils.event.EventListener;
import pers.zyc.tools.utils.lifecycle.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import static pers.zyc.tools.redis.client.ResponseCast.STRING;

/**
 * @author zhangyancheng
 */
class ConnectionPool extends Service {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);

	private final ClientConfig config;
	private final NetWorkGroup netWorkGroup;
	private final Responder responder;
	private final GenericObjectPool<Connection> pool;
	private final Retriever retriever = new Retriever();

	ConnectionPool(ClientConfig config) {
		this.config = config;
		responder = new Responder(config);
		netWorkGroup = new NetWorkGroup(config.getNetWorkers());

		pool = new GenericObjectPool<>(new ConnectionFactory(), config.createPoolConfig());
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
		responder.stop();
		netWorkGroup.stop();
	}

	Connection getConnection() {
		try {
			return pool.borrowObject();
		} catch (Exception e) {
			throw new RedisClientException("Could not get a connection from the pool", e);
		}
	}

	private class Retriever implements EventListener<ConnectionEvent> {

		@Override
		public void onEvent(ConnectionEvent event) {
			switch (event.eventType) {
				case REQUEST_SET:
				case REQUEST_SEND:
				case CONNECTION_CLOSED:
					return;
				case RESPONSE_RECEIVED:
					pool.returnObject(event.getSource());
					break;
				case REQUEST_TIMEOUT:
				case EXCEPTION_CAUGHT:
					try {
						pool.invalidateObject(event.getSource());
					} catch (Exception e) {
						LOGGER.error("Invalidate connection exception!", e);
					}
					break;
			}
		}
	}

	private class ConnectionFactory implements PooledObjectFactory<Connection> {

		@Override
		public PooledObject<Connection> makeObject() throws Exception {
			if (netWorkGroup.inNetworking()) {
				throw new RedisClientException("Can't create connection in networking!");
			}

			SocketChannel channel = createChannel(config.getHost(), config.getPort(), config.getConnectionTimeout());

			Connection connection = new Connection(channel, netWorkGroup.next());
			connection.addListener(responder);

			if (config.getPassword() != null) {
				connection.send(new Auth(config.getPassword()), STRING).get();
			}
			if (config.getDb() > 0) {
				connection.send(new Select(config.getDb()), STRING).get();
			}

			connection.addListener(retriever);
			return new DefaultPooledObject<>(connection);
		}

		@Override
		public void destroyObject(PooledObject<Connection> p) throws Exception {
			try (Connection connection = p.getObject()) {
				connection.removeListener(retriever);

				if (!netWorkGroup.inNetworking() && connection.channel.isConnected()) {
					connection.send(new Quit(), STRING).get();
				}
			}
		}

		@Override
		public boolean validateObject(PooledObject<Connection> p) {
			Connection connection = p.getObject();
			if (connection.channel.isConnected()) {
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
