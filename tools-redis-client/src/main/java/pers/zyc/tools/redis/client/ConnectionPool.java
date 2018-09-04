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
import pers.zyc.tools.redis.client.util.ResponsePromise;
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

import static pers.zyc.tools.redis.client.ResponseCast.STRING;

/**
 * @author zhangyancheng
 */
class ConnectionPool extends ThreadService implements EventListener<ConnectionEvent>, PooledObjectFactory<Connection> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);

	private final ClientConfig config;
	private final NetWorkGroup netWorkGroup;
	private final GenericObjectPool<Connection> pool;
	private final ConcurrentMap<Connection, ResponsePromise<?>> requestingMap = new ConcurrentHashMap<>();

	ConnectionPool(ClientConfig config) {
		this.config = config;
		netWorkGroup = new NetWorkGroup(config.getNetWorkers());

		pool = new GenericObjectPool<>(this, config.<Connection>createPoolConfig());
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
		//如果仍有请求中的连接, 则等待请求结束
		while (pool.getNumActive() > 0) {
			//等待一个超时清理周期, 则所有请求必定都结束了
			TimeUnit.MILLISECONDS.sleep(config.getRequestTimeoutDetectInterval());
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
				for (Map.Entry<Connection, ResponsePromise<?>> entry : requestingMap.entrySet()) {
					if (entry.getValue().getCreateTime() + config.getRequestTimeout() <= TimeMillis.INSTANCE.get()) {
						entry.getKey().timeout();
					}
				}
			}
		};
	}

	Connection getConnection() {
		try {
			Connection connection = pool.borrowObject();
			connection.allocated = true;
			return connection;
		} catch (Exception e) {
			throw new RedisClientException("Could not get a connection from the pool", e);
		}
	}

	private void recycleConnection(Connection connection, boolean invalid) {
		if (invalid) {
			try {
				pool.invalidateObject(connection);
			} catch (Exception e) {
				LOGGER.warn("Invalidate connection exception!", e);
			}
		} else {
			pool.returnObject(connection);
		}
	}

	@Override
	public void onEvent(ConnectionEvent event) {
		final Connection connection = event.getSource();
		final Object response;

		switch (event.eventType) {
			case REQUEST_SET:
				requestingMap.put(connection, (ResponsePromise<?>) event.payload());
				return;
			case EXCEPTION_CAUGHT:
			case RESPONSE_RECEIVED:
			case REQUEST_TIMEOUT:
				//请求结果事件
				response = event.payload();
				break;
			default:
				return;
		}
		Promise<?> responsePromise = requestingMap.remove(connection);
		if (responsePromise == null) {
			assert !connection.allocated && !connection.healthy;
			//未分配状态时检测到连接异常(被对端关闭, 触发了Channel上的读事件), 销毁连接
			recycleConnection(connection, true);
		} else {
			responsePromise.response(response);

			if (connection.allocated) {
				//修改状态, 控制每个连接借出后只回收一次
				connection.allocated = false;
				//回收连接, 如果为异常响应则销毁连接
				recycleConnection(connection, response instanceof Exception);
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
		connection.addListener(this);
		try {
			if (config.getPassword() != null) {
				connection.send(new Auth(config.getPassword()), STRING).get();
			}
			if (config.getDb() > 0) {
				connection.send(new Select(config.getDb()), STRING).get();
			}
			LOGGER.debug("Created new {}", connection);
			return new DefaultPooledObject<>(connection);
		} catch (Exception e) {
			connection.close();
			throw e;
		}
	}

	@Override
	public void destroyObject(PooledObject<Connection> p) throws Exception {
		try (Connection connection = p.getObject()) {
			if (connection.healthy && !netWorkGroup.inNetworking()) {
				connection.send(new Quit(), STRING).get();
			}
		}
	}

	@Override
	public boolean validateObject(PooledObject<Connection> p) {
		Connection connection = p.getObject();
		if (connection.healthy) {
			try {
				return connection.send(new Ping(), STRING).get().equals("PONG");
			} catch (Exception e) {
				LOGGER.warn("PING-PONG failed.", e.getCause());
			}
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
}
