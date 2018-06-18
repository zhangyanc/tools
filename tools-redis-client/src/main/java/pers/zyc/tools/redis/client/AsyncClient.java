package pers.zyc.tools.redis.client;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import pers.zyc.tools.lifecycle.Service;
import pers.zyc.tools.redis.client.request.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static pers.zyc.tools.redis.client.ResponseCast.*;

/**
 * @author zhangyancheng
 */
public class AsyncClient extends Service implements AsyncCommands {
	public static final int DEFAULT_CONNECTION_TIMEOUT = -1;
	public static final int DEFAULT_REQUEST_TIMEOUT = -1;
	public static final int DEFAULT_NET_WORKERS = 1;

	private final NetWorker[] netWorkers;
	private final ConnectionPool connectionPool;
	private final RequestExecutor requestExecutor;

	public AsyncClient(String connectStr) throws Exception {
		this(connectStr, DEFAULT_NET_WORKERS, new GenericObjectPoolConfig());
	}

	public AsyncClient(String connectStr,
							 int workers,
							 GenericObjectPoolConfig poolConfig) throws Exception {
		this(connectStr, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_REQUEST_TIMEOUT, workers, poolConfig);
	}

	public AsyncClient(String connectStr,
							 int timeout,
							 int workers) throws Exception {
		this(connectStr, timeout, timeout, workers, new GenericObjectPoolConfig());
	}

	public AsyncClient(String connectStr,
							 int connectionTimeout,
							 int requestTimeout,
							 int workers,
							 GenericObjectPoolConfig poolConfig) throws Exception {
		if (workers <= 0) {
			throw new IllegalArgumentException(String.format("workers: %d (expected: > 0)", workers));
		}

		URI uri = URI.create(connectStr);
		this.netWorkers = new NetWorker[workers];
		for (int i = 0; i < workers; i++) {
			netWorkers[i] = new NetWorker(requestTimeout);
		}
		connectionPool = new ConnectionPool(new ConnectionFactory(uri.getHost(),
				uri.getPort(), connectionTimeout), poolConfig);
		requestExecutor = new RequestExecutor(connectionPool);
	}

	private NetWorker chooseNetWorker(int connectionId) {
		return netWorkers[connectionId % netWorkers.length];
	}

	private class ConnectionFactory implements PooledObjectFactory<Connection> {

		private final String host;
		private final int port, connectionTimeout;
		private final AtomicInteger connectionId = new AtomicInteger();

		ConnectionFactory(String host, int port, int connectionTimeout) {
			this.host = host;
			this.port = port;
			this.connectionTimeout = connectionTimeout;
		}

		@Override
		public PooledObject<Connection> makeObject() throws Exception {
			SocketChannel channel = createChannel(host, port, connectionTimeout);

			Connection connection = new Connection(channel);
			connection.addListener(requestExecutor);
			connection.addListener(connectionPool);

			NetWorker netWorker = chooseNetWorker(connectionId.incrementAndGet());
			connection.addListener(netWorker);
			netWorker.register(connection);

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

	@Override
	protected void doStart() {
		for (NetWorker netWorker : netWorkers) {
			netWorker.start();
		}
	}

	@Override
	protected void doStop() throws Exception {
		connectionPool.close();
		for (NetWorker netWorker : netWorkers) {
			netWorker.stop();
		}
	}

	@Override
	public ResponseFuture<String> _set(String key, String value) {
		return requestExecutor.execute(new pers.zyc.tools.redis.client.request.Set(key, value), STRING);
	}

	@Override
	public ResponseFuture<String> _set(String key, String value, String nxxx, String expx, long time) {
		return requestExecutor.execute(new pers.zyc.tools.redis.client.request.Set(key, value, nxxx, expx, time), STRING);
	}

	@Override
	public ResponseFuture<String> _set(String key, String value, String nxxx) {
		return requestExecutor.execute(new pers.zyc.tools.redis.client.request.Set(key, value, nxxx), STRING);
	}

	@Override
	public ResponseFuture<String> _get(String key) {
		return requestExecutor.execute(new Get(key), STRING);
	}

	@Override
	public ResponseFuture<Boolean> _exists(String key) {
		return requestExecutor.execute(new Exists(key), BOOLEAN);
	}

	@Override
	public ResponseFuture<Long> _persist(String key) {
		return requestExecutor.execute(new Persist(key), LONG);
	}

	@Override
	public ResponseFuture<String> _type(String key) {
		return requestExecutor.execute(new Type(key), STRING);
	}

	@Override
	public ResponseFuture<Long> _expire(String key, int seconds) {
		return requestExecutor.execute(new Expire(key, seconds), LONG);
	}

	@Override
	public ResponseFuture<Long> _pexpire(String key, long milliseconds) {
		return requestExecutor.execute(new PExpire(key, milliseconds), LONG);
	}

	@Override
	public ResponseFuture<Long> _expireAt(String key, long unixTime) {
		return requestExecutor.execute(new ExpireAt(key, unixTime), LONG);
	}

	@Override
	public ResponseFuture<Long> _pexpireAt(String key, long millisecondsTimestamp) {
		return requestExecutor.execute(new PExpireAt(key, millisecondsTimestamp), LONG);
	}

	@Override
	public ResponseFuture<Long> _ttl(String key) {
		return requestExecutor.execute(new Ttl(key), LONG);
	}

	@Override
	public ResponseFuture<Long> _pttl(String key) {
		return requestExecutor.execute(new PTtl(key), LONG);
	}

	@Override
	public ResponseFuture<Boolean> _setbit(String key, long offset, boolean value) {
		return requestExecutor.execute(new SetBit(key, offset, value), BOOLEAN);
	}

	@Override
	public ResponseFuture<Boolean> _setbit(String key, long offset, String value) {
		return requestExecutor.execute(new SetBit(key, offset, value), BOOLEAN);
	}

	@Override
	public ResponseFuture<Boolean> _getbit(String key, long offset) {
		return requestExecutor.execute(new GetBit(key, offset), BOOLEAN);
	}

	@Override
	public ResponseFuture<Long> _setrange(String key, long offset, String value) {
		return requestExecutor.execute(new SetRange(key, offset, value), LONG);
	}

	@Override
	public ResponseFuture<String> _getrange(String key, long startOffset, long endOffset) {
		return requestExecutor.execute(new GetRange(key, startOffset, endOffset), STRING);
	}

	@Override
	public ResponseFuture<String> _getSet(String key, String value) {
		return requestExecutor.execute(new GetSet(key, value), STRING);
	}

	@Override
	public ResponseFuture<Long> _setnx(String key, String value) {
		return requestExecutor.execute(new SetNx(key, value), LONG);
	}

	@Override
	public ResponseFuture<String> _setex(String key, int seconds, String value) {
		return requestExecutor.execute(new SetEx(key, seconds, value), STRING);
	}

	@Override
	public ResponseFuture<String> _psetex(String key, long milliseconds, String value) {
		return requestExecutor.execute(new PSetEx(key, milliseconds, value), STRING);
	}

	@Override
	public ResponseFuture<Long> _decrBy(String key, long integer) {
		return requestExecutor.execute(new DecrementBy(key, integer), LONG);
	}

	@Override
	public ResponseFuture<Long> _decr(String key) {
		return requestExecutor.execute(new Decrement(key), LONG);
	}

	@Override
	public ResponseFuture<Long> _incrBy(String key, long integer) {
		return requestExecutor.execute(new IncrementBy(key, integer), LONG);
	}

	@Override
	public ResponseFuture<Double> _incrByFloat(String key, double value) {
		return requestExecutor.execute(new IncrementByFloat(key, value), DOUBLE);
	}

	@Override
	public ResponseFuture<Long> _incr(String key) {
		return requestExecutor.execute(new Increment(key), LONG);
	}

	@Override
	public ResponseFuture<Long> _append(String key, String value) {
		return requestExecutor.execute(new Append(key, value), LONG);
	}

	@Override
	public ResponseFuture<String> _substr(String key, int start, int end) {
		return requestExecutor.execute(new SubStr(key, start, end), STRING);
	}

	@Override
	public ResponseFuture<Long> _hset(String key, String field, String value) {
		return requestExecutor.execute(new HSet(key, field, value), LONG);
	}

	@Override
	public ResponseFuture<String> _hget(String key, String field) {
		return requestExecutor.execute(new HGet(key, field), STRING);
	}

	@Override
	public ResponseFuture<Long> _hsetnx(String key, String field, String value) {
		return requestExecutor.execute(new HSetNx(key, field, value), LONG);
	}

	@Override
	public ResponseFuture<String> _hmset(String key, Map<String, String> hash) {
		return requestExecutor.execute(new HMSet(key, hash), STRING);
	}

	@Override
	public ResponseFuture<List<String>> _hmget(String key, String... fields) {
		return requestExecutor.execute(new HMGet(key, fields), STRING_LIST);
	}

	@Override
	public ResponseFuture<Long> _hincrBy(String key, String field, long value) {
		return requestExecutor.execute(new HIncrementBy(key, field, value), LONG);
	}

	@Override
	public ResponseFuture<Double> _hincrByFloat(String key, String field, double value) {
		return requestExecutor.execute(new HIncrementByFloat(key, field, value), DOUBLE);
	}

	@Override
	public ResponseFuture<Boolean> _hexists(String key, String field) {
		return requestExecutor.execute(new HExists(key, field), BOOLEAN);
	}

	@Override
	public ResponseFuture<Long> _hdel(String key, String... field) {
		return requestExecutor.execute(new HDelete(key, field), LONG);
	}

	@Override
	public ResponseFuture<Long> _hlen(String key) {
		return null;
	}

	@Override
	public ResponseFuture<Set<String>> _hkeys(String key) {
		return null;
	}

	@Override
	public ResponseFuture<List<String>> _hvals(String key) {
		return null;
	}

	@Override
	public ResponseFuture<Map<String, String>> _hgetAll(String key) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _rpush(String key, String... string) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _lpush(String key, String... string) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _llen(String key) {
		return null;
	}

	@Override
	public ResponseFuture<List<String>> _lrange(String key, long start, long end) {
		return null;
	}

	@Override
	public ResponseFuture<String> _ltrim(String key, long start, long end) {
		return null;
	}

	@Override
	public ResponseFuture<String> _lindex(String key, long index) {
		return null;
	}

	@Override
	public ResponseFuture<String> _lset(String key, long index, String value) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _lrem(String key, long count, String value) {
		return null;
	}

	@Override
	public ResponseFuture<String> _lpop(String key) {
		return null;
	}

	@Override
	public ResponseFuture<String> _rpop(String key) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _sadd(String key, String... member) {
		return null;
	}

	@Override
	public ResponseFuture<Set<String>> _smembers(String key) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _srem(String key, String... member) {
		return null;
	}

	@Override
	public ResponseFuture<String> _spop(String key) {
		return null;
	}

	@Override
	public ResponseFuture<Set<String>> _spop(String key, long count) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _scard(String key) {
		return null;
	}

	@Override
	public ResponseFuture<Boolean> _sismember(String key, String member) {
		return null;
	}

	@Override
	public ResponseFuture<String> _srandmember(String key) {
		return null;
	}

	@Override
	public ResponseFuture<List<String>> _srandmember(String key, int count) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _strlen(String key) {
		return requestExecutor.execute(new StrLen(key), LONG);
	}

	@Override
	public ResponseFuture<Long> _zadd(String key, double score, String member) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _zadd(String key, Map<String, Double> _scoreMembers) {
		return null;
	}

	@Override
	public ResponseFuture<Set<String>> _zrange(String key, long start, long end) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _zrem(String key, String... member) {
		return null;
	}

	@Override
	public ResponseFuture<Double> _zincrby(String key, double score, String member) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _zrank(String key, String member) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _zrevrank(String key, String member) {
		return null;
	}

	@Override
	public ResponseFuture<Set<String>> _zrevrange(String key, long start, long end) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _zcard(String key) {
		return null;
	}

	@Override
	public ResponseFuture<Double> _zscore(String key, String member) {
		return null;
	}

	@Override
	public ResponseFuture<List<String>> _sort(String key) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _zcount(String key, double min, double max) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _zcount(String key, String min, String max) {
		return null;
	}

	@Override
	public ResponseFuture<Set<String>> _zrangeByScore(String key, double min, double max) {
		return null;
	}

	@Override
	public ResponseFuture<Set<String>> _zrangeByScore(String key, String min, String max) {
		return null;
	}

	@Override
	public ResponseFuture<Set<String>> _zrevrangeByScore(String key, double max, double min) {
		return null;
	}

	@Override
	public ResponseFuture<Set<String>> _zrangeByScore(String key, double min, double max, int offset, int count) {
		return null;
	}

	@Override
	public ResponseFuture<Set<String>> _zrevrangeByScore(String key, String max, String min) {
		return null;
	}

	@Override
	public ResponseFuture<Set<String>> _zrangeByScore(String key, String min, String max, int offset, int count) {
		return null;
	}

	@Override
	public ResponseFuture<Set<String>> _zrevrangeByScore(String key, double max, double min, int offset, int count) {
		return null;
	}

	@Override
	public ResponseFuture<Set<String>> _zrevrangeByScore(String key, String max, String min, int offset, int count) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _zremrangeByRank(String key, long start, long end) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _zremrangeByScore(String key, double start, double end) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _zremrangeByScore(String key, String start, String end) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _zlexcount(String key, String min, String max) {
		return null;
	}

	@Override
	public ResponseFuture<Set<String>> _zrangeByLex(String key, String min, String max) {
		return null;
	}

	@Override
	public ResponseFuture<Set<String>> _zrangeByLex(String key, String min, String max, int offset, int count) {
		return null;
	}

	@Override
	public ResponseFuture<Set<String>> _zrevrangeByLex(String key, String max, String min) {
		return null;
	}

	@Override
	public ResponseFuture<Set<String>> _zrevrangeByLex(String key, String max, String min, int offset, int count) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _zremrangeByLex(String key, String min, String max) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _lpushx(String key, String... string) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _rpushx(String key, String... string) {
		return null;
	}

	@Override
	public ResponseFuture<List<String>> _blpop(String arg) {
		return null;
	}

	@Override
	public ResponseFuture<List<String>> _blpop(int timeout, String key) {
		return null;
	}

	@Override
	public ResponseFuture<List<String>> _brpop(String arg) {
		return null;
	}

	@Override
	public ResponseFuture<List<String>> _brpop(int timeout, String key) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _del(String key) {
		return requestExecutor.execute(new Delete(key), LONG);
	}

	@Override
	public ResponseFuture<String> _echo(String string) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _move(String key, int dbIndex) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _bitcount(String key) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _bitcount(String key, long start, long end) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _bitpos(String key, boolean value) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _pfadd(String key, String... elements) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _pfcount(String key) {
		return null;
	}

	@Override
	public ResponseFuture<Long> _geoadd(String key, double longitude, double latitude, String member) {
		return null;
	}

	@Override
	public ResponseFuture<Double> _geodist(String key, String member1, String member2) {
		return null;
	}

	@Override
	public ResponseFuture<List<String>> _geohash(String key, String... members) {
		return null;
	}

	@Override
	public ResponseFuture<List<Long>> _bitfield(String key, String... arguments) {
		return null;
	}
}
