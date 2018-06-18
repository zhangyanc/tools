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
public class CustomRedisClient extends Service implements RedisClient {
	public static final int DEFAULT_CONNECTION_TIMEOUT = -1;
	public static final int DEFAULT_REQUEST_TIMEOUT = -1;
	public static final int DEFAULT_NET_WORKERS = 1;

	private final NetWorker[] netWorkers;
	private final ConnectionPool connectionPool;
	private final RequestExecutor requestExecutor;

	public CustomRedisClient(String connectStr) throws Exception {
		this(connectStr, DEFAULT_NET_WORKERS, new GenericObjectPoolConfig());
	}

	public CustomRedisClient(String connectStr,
							 int workers,
							 GenericObjectPoolConfig poolConfig) throws Exception {
		this(connectStr, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_REQUEST_TIMEOUT, workers, poolConfig);
	}

	public CustomRedisClient(String connectStr,
							 int timeout,
							 int workers) throws Exception {
		this(connectStr, timeout, timeout, workers, new GenericObjectPoolConfig());
	}

	public CustomRedisClient(String connectStr,
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
	public String set(String key, String value) {
		return requestExecutor.execute(new pers.zyc.tools.redis.client.request.Set(key, value), STRING)
				.get();
	}

	@Override
	public String set(String key, String value, String nxxx, String expx, long time) {
		return requestExecutor.execute(new pers.zyc.tools.redis.client.request.Set(key, value, nxxx, expx, time), STRING)
				.get();
	}

	@Override
	public String set(String key, String value, String nxxx) {
		return requestExecutor.execute(new pers.zyc.tools.redis.client.request.Set(key, value, nxxx), STRING)
				.get();
	}

	@Override
	public String get(String key) {
		return requestExecutor.execute(new Get(key), STRING)
				.get();
	}

	@Override
	public Boolean exists(String key) {
		return requestExecutor.execute(new Exists(key), BOOLEAN)
				.get();
	}

	@Override
	public Long persist(String key) {
		return requestExecutor.execute(new Persist(key), LONG)
				.get();
	}

	@Override
	public String type(String key) {
		return requestExecutor.execute(new Type(key), STRING)
				.get();
	}

	@Override
	public Long expire(String key, int seconds) {
		return requestExecutor.execute(new Expire(key, seconds), LONG)
				.get();
	}

	@Override
	public Long pexpire(String key, long milliseconds) {
		return requestExecutor.execute(new PExpire(key, milliseconds), LONG)
				.get();
	}

	@Override
	public Long expireAt(String key, long unixTime) {
		return requestExecutor.execute(new ExpireAt(key, unixTime), LONG)
				.get();
	}

	@Override
	public Long pexpireAt(String key, long millisecondsTimestamp) {
		return requestExecutor.execute(new PExpireAt(key, millisecondsTimestamp), LONG)
				.get();
	}

	@Override
	public Long ttl(String key) {
		return requestExecutor.execute(new Ttl(key), LONG)
				.get();
	}

	@Override
	public Long pttl(String key) {
		return requestExecutor.execute(new PTtl(key), LONG)
				.get();
	}

	@Override
	public Boolean setbit(String key, long offset, boolean value) {
		return requestExecutor.execute(new SetBit(key, offset, value), BOOLEAN)
				.get();
	}

	@Override
	public Boolean setbit(String key, long offset, String value) {
		return requestExecutor.execute(new SetBit(key, offset, value), BOOLEAN)
				.get();
	}

	@Override
	public Boolean getbit(String key, long offset) {
		return requestExecutor.execute(new GetBit(key, offset), BOOLEAN)
				.get();
	}

	@Override
	public Long setrange(String key, long offset, String value) {
		return requestExecutor.execute(new SetRange(key, offset, value), LONG)
				.get();
	}

	@Override
	public String getrange(String key, long startOffset, long endOffset) {
		return requestExecutor.execute(new GetRange(key, startOffset, endOffset), STRING)
				.get();
	}

	@Override
	public String getSet(String key, String value) {
		return requestExecutor.execute(new GetSet(key, value), STRING)
				.get();
	}

	@Override
	public Long setnx(String key, String value) {
		return requestExecutor.execute(new SetNx(key, value), LONG)
				.get();
	}

	@Override
	public String setex(String key, int seconds, String value) {
		return requestExecutor.execute(new SetEx(key, seconds, value), STRING)
				.get();
	}

	@Override
	public String psetex(String key, long milliseconds, String value) {
		return requestExecutor.execute(new PSetEx(key, milliseconds, value), STRING)
				.get();
	}

	@Override
	public Long decrBy(String key, long integer) {
		return requestExecutor.execute(new DecrementBy(key, integer), LONG)
				.get();
	}

	@Override
	public Long decr(String key) {
		return requestExecutor.execute(new Decrement(key), LONG)
				.get();
	}

	@Override
	public Long incrBy(String key, long integer) {
		return requestExecutor.execute(new IncrementBy(key, integer), LONG)
				.get();
	}

	@Override
	public Double incrByFloat(String key, double value) {
		return requestExecutor.execute(new IncrementByFloat(key, value), DOUBLE)
				.get();
	}

	@Override
	public Long incr(String key) {
		return requestExecutor.execute(new Increment(key), LONG)
				.get();
	}

	@Override
	public Long append(String key, String value) {
		return requestExecutor.execute(new Append(key, value), LONG)
				.get();
	}

	@Override
	public String substr(String key, int start, int end) {
		return requestExecutor.execute(new SubStr(key, start, end), STRING)
				.get();
	}

	@Override
	public Long hset(String key, String field, String value) {
		return requestExecutor.execute(new HSet(key, field, value), LONG)
				.get();
	}

	@Override
	public String hget(String key, String field) {
		return requestExecutor.execute(new HGet(key, field), STRING)
				.get();
	}

	@Override
	public Long hsetnx(String key, String field, String value) {
		return requestExecutor.execute(new HSetNx(key, field, value), LONG)
				.get();
	}

	@Override
	public String hmset(String key, Map<String, String> hash) {
		return requestExecutor.execute(new HMSet(key, hash), STRING)
				.get();
	}

	@Override
	public List<String> hmget(String key, String... fields) {
		return requestExecutor.execute(new HMGet(key, fields), STRING_LIST)
				.get();
	}

	@Override
	public Long hincrBy(String key, String field, long value) {
		return requestExecutor.execute(new HIncrementBy(key, field, value), LONG)
				.get();
	}

	@Override
	public Double hincrByFloat(String key, String field, double value) {
		return requestExecutor.execute(new HIncrementByFloat(key, field, value), DOUBLE)
				.get();
	}

	@Override
	public Boolean hexists(String key, String field) {
		return requestExecutor.execute(new HExists(key, field), BOOLEAN)
				.get();
	}

	@Override
	public Long hdel(String key, String... field) {
		return requestExecutor.execute(new HDelete(key, field), LONG)
				.get();
	}

	@Override
	public Long hlen(String key) {
		return null;
	}

	@Override
	public Set<String> hkeys(String key) {
		return null;
	}

	@Override
	public List<String> hvals(String key) {
		return null;
	}

	@Override
	public Map<String, String> hgetAll(String key) {
		return null;
	}

	@Override
	public Long rpush(String key, String... string) {
		return null;
	}

	@Override
	public Long lpush(String key, String... string) {
		return null;
	}

	@Override
	public Long llen(String key) {
		return null;
	}

	@Override
	public List<String> lrange(String key, long start, long end) {
		return null;
	}

	@Override
	public String ltrim(String key, long start, long end) {
		return null;
	}

	@Override
	public String lindex(String key, long index) {
		return null;
	}

	@Override
	public String lset(String key, long index, String value) {
		return null;
	}

	@Override
	public Long lrem(String key, long count, String value) {
		return null;
	}

	@Override
	public String lpop(String key) {
		return null;
	}

	@Override
	public String rpop(String key) {
		return null;
	}

	@Override
	public Long sadd(String key, String... member) {
		return null;
	}

	@Override
	public Set<String> smembers(String key) {
		return null;
	}

	@Override
	public Long srem(String key, String... member) {
		return null;
	}

	@Override
	public String spop(String key) {
		return null;
	}

	@Override
	public Set<String> spop(String key, long count) {
		return null;
	}

	@Override
	public Long scard(String key) {
		return null;
	}

	@Override
	public Boolean sismember(String key, String member) {
		return null;
	}

	@Override
	public String srandmember(String key) {
		return null;
	}

	@Override
	public List<String> srandmember(String key, int count) {
		return null;
	}

	@Override
	public Long strlen(String key) {
		return requestExecutor.execute(new StrLen(key), LONG).get();
	}

	@Override
	public Long zadd(String key, double score, String member) {
		return null;
	}

	/*@Override
	public Long zadd(String key, double score, String member, ZAddParams params) {
		return null;
	}*/

	@Override
	public Long zadd(String key, Map<String, Double> scoreMembers) {
		return null;
	}

	/*@Override
	public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
		return null;
	}*/

	@Override
	public Set<String> zrange(String key, long start, long end) {
		return null;
	}

	@Override
	public Long zrem(String key, String... member) {
		return null;
	}

	@Override
	public Double zincrby(String key, double score, String member) {
		return null;
	}

	/*@Override
	public Double zincrby(String key, double score, String member, ZIncrByParams params) {
		return null;
	}*/

	@Override
	public Long zrank(String key, String member) {
		return null;
	}

	@Override
	public Long zrevrank(String key, String member) {
		return null;
	}

	@Override
	public Set<String> zrevrange(String key, long start, long end) {
		return null;
	}

	/*@Override
	public Set<Tuple> zrangeWithScores(String key, long start, long end) {
		return null;
	}*/

	/*@Override
	public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
		return null;
	}*/

	@Override
	public Long zcard(String key) {
		return null;
	}

	@Override
	public Double zscore(String key, String member) {
		return null;
	}

	@Override
	public List<String> sort(String key) {
		return null;
	}

	/*@Override
	public List<String> sort(String key, SortingParams sortingParameters) {
		return null;
	}*/

	@Override
	public Long zcount(String key, double min, double max) {
		return null;
	}

	@Override
	public Long zcount(String key, String min, String max) {
		return null;
	}

	@Override
	public Set<String> zrangeByScore(String key, double min, double max) {
		return null;
	}

	@Override
	public Set<String> zrangeByScore(String key, String min, String max) {
		return null;
	}

	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min) {
		return null;
	}

	@Override
	public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
		return null;
	}

	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min) {
		return null;
	}

	@Override
	public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
		return null;
	}

	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
		return null;
	}

	/*@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
		return null;
	}*/

	/*@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
		return null;
	}*/

	/*@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
		return null;
	}*/

	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
		return null;
	}

	/*@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
		return null;
	}*/

	/*@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
		return null;
	}*/

	/*@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
		return null;
	}*/

	/*@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
		return null;
	}*/

	/*@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
		return null;
	}*/

	@Override
	public Long zremrangeByRank(String key, long start, long end) {
		return null;
	}

	@Override
	public Long zremrangeByScore(String key, double start, double end) {
		return null;
	}

	@Override
	public Long zremrangeByScore(String key, String start, String end) {
		return null;
	}

	@Override
	public Long zlexcount(String key, String min, String max) {
		return null;
	}

	@Override
	public Set<String> zrangeByLex(String key, String min, String max) {
		return null;
	}

	@Override
	public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
		return null;
	}

	@Override
	public Set<String> zrevrangeByLex(String key, String max, String min) {
		return null;
	}

	@Override
	public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
		return null;
	}

	@Override
	public Long zremrangeByLex(String key, String min, String max) {
		return null;
	}

	/*@Override
	public Long linsert(String key, BinaryClient.LIST_POSITION where, String pivot, String value) {
		return null;
	}*/

	@Override
	public Long lpushx(String key, String... string) {
		return null;
	}

	@Override
	public Long rpushx(String key, String... string) {
		return null;
	}

	@Override
	public List<String> blpop(String arg) {
		return null;
	}

	@Override
	public List<String> blpop(int timeout, String key) {
		return null;
	}

	@Override
	public List<String> brpop(String arg) {
		return null;
	}

	@Override
	public List<String> brpop(int timeout, String key) {
		return null;
	}

	@Override
	public Long del(String key) {
		return requestExecutor.execute(new Delete(key), LONG)
				.get();
	}

	@Override
	public String echo(String string) {
		return null;
	}

	@Override
	public Long move(String key, int dbIndex) {
		return null;
	}

	@Override
	public Long bitcount(String key) {
		return null;
	}

	@Override
	public Long bitcount(String key, long start, long end) {
		return null;
	}

	@Override
	public Long bitpos(String key, boolean value) {
		return null;
	}

	/*@Override
	public Long bitpos(String key, boolean value, BitPosParams params) {
		return null;
	}*/

	/*@Override
	public ScanResult<Map.Entry<String, String>> hscan(String key, int cursor) {
		return null;
	}*/

	/*@Override
	public ScanResult<String> sscan(String key, int cursor) {
		return null;
	}*/

	/*@Override
	public ScanResult<Tuple> zscan(String key, int cursor) {
		return null;
	}*/

	/*@Override
	public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor) {
		return null;
	}*/

	/*@Override
	public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
		return null;
	}*/

	/*@Override
	public ScanResult<String> sscan(String key, String cursor) {
		return null;
	}*/

	/*@Override
	public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
		return null;
	}*/

	/*@Override
	public ScanResult<Tuple> zscan(String key, String cursor) {
		return null;
	}*/

	/*@Override
	public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
		return null;
	}*/

	@Override
	public Long pfadd(String key, String... elements) {
		return null;
	}

	@Override
	public long pfcount(String key) {
		return 0;
	}

	@Override
	public Long geoadd(String key, double longitude, double latitude, String member) {
		return null;
	}

	/*@Override
	public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
		return null;
	}*/

	@Override
	public Double geodist(String key, String member1, String member2) {
		return null;
	}

	/*@Override
	public Double geodist(String key, String member1, String member2, GeoUnit unit) {
		return null;
	}*/

	@Override
	public List<String> geohash(String key, String... members) {
		return null;
	}

	/*@Override
	public List<GeoCoordinate> geopos(String key, String... members) {
		return null;
	}*/

	/*@Override
	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit) {
		return null;
	}*/

	/*@Override
	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
		return null;
	}*/

	/*@Override
	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
		return null;
	}*/

	/*@Override
	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
		return null;
	}*/

	@Override
	public List<Long> bitfield(String key, String... arguments) {
		return null;
	}
}
