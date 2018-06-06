package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.request.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import static pers.zyc.tools.redis.client.ResponseCast.*;

/**
 * @author zhangyancheng
 */
public class CustomRedisClient implements RedisClient {

	private final ConnectionPool connectionPool;
	private final long timeout;

	public CustomRedisClient(ConnectionPool connectionPool) {
		this.connectionPool = connectionPool;
		this.timeout = connectionPool.getRequestTimeout();
	}


	@Override
	public String set(String key, String value) {
		return new RequestAction<String>()
				.request(new pers.zyc.tools.redis.client.request.Set(key, value))
				.connection(connectionPool.getConnection())
				.responseCast(STRING)
				.execute()
				.get(timeout);
	}

	@Override
	public String set(String key, String value, String nxxx, String expx, long time) {
		return new RequestAction<String>()
				.request(new pers.zyc.tools.redis.client.request.Set(key, value, nxxx, expx, time))
				.connection(connectionPool.getConnection())
				.responseCast(STRING)
				.execute()
				.get(timeout);
	}

	@Override
	public String set(String key, String value, String nxxx) {
		return new RequestAction<String>()
				.request(new pers.zyc.tools.redis.client.request.Set(key, value, nxxx))
				.connection(connectionPool.getConnection())
				.responseCast(STRING)
				.execute()
				.get(timeout);
	}

	@Override
	public String get(String key) {
		return new RequestAction<String>()
				.request(new Get(key))
				.connection(connectionPool.getConnection())
				.responseCast(STRING)
				.execute()
				.get(timeout);
	}

	@Override
	public Boolean exists(String key) {
		return new RequestAction<Boolean>()
				.request(new Exists(key))
				.connection(connectionPool.getConnection())
				.responseCast(BOOLEAN)
				.execute()
				.get(timeout);
	}

	@Override
	public Long persist(String key) {
		return new RequestAction<Long>()
				.request(new Persist(key))
				.connection(connectionPool.getConnection())
				.responseCast(LONG)
				.execute()
				.get(timeout);
	}

	@Override
	public String type(String key) {
		return new RequestAction<String>()
				.request(new Type(key))
				.connection(connectionPool.getConnection())
				.responseCast(STRING)
				.execute()
				.get(timeout);
	}

	@Override
	public Long expire(String key, int seconds) {
		return new RequestAction<Long>()
				.request(new Expire(key, seconds))
				.connection(connectionPool.getConnection())
				.responseCast(LONG)
				.execute()
				.get(timeout);
	}

	@Override
	public Long pexpire(String key, long milliseconds) {
		return new RequestAction<Long>()
				.request(new PExpire(key, milliseconds))
				.connection(connectionPool.getConnection())
				.responseCast(LONG)
				.execute()
				.get(timeout);
	}

	@Override
	public Long expireAt(String key, long unixTime) {
		return new RequestAction<Long>()
				.request(new ExpireAt(key, unixTime))
				.connection(connectionPool.getConnection())
				.responseCast(LONG).execute()
				.get(timeout);
	}

	@Override
	public Long pexpireAt(String key, long millisecondsTimestamp) {
		return new RequestAction<Long>()
				.request(new PExpireAt(key, millisecondsTimestamp))
				.connection(connectionPool.getConnection())
				.responseCast(LONG)
				.execute()
				.get(timeout);
	}

	@Override
	public Long ttl(String key) {
		return new RequestAction<Long>()
				.request(new Ttl(key))
				.connection(connectionPool.getConnection())
				.responseCast(LONG)
				.execute()
				.get(timeout);
	}

	@Override
	public Long pttl(String key) {
		return new RequestAction<Long>()
				.request(new PTtl(key))
				.connection(connectionPool.getConnection())
				.responseCast(LONG)
				.execute()
				.get(timeout);
	}

	@Override
	public Boolean setbit(String key, long offset, boolean value) {
		return new RequestAction<Boolean>()
				.request(new SetBit(key, offset, value))
				.connection(connectionPool.getConnection())
				.responseCast(BOOLEAN)
				.execute()
				.get(timeout);
	}

	@Override
	public Boolean setbit(String key, long offset, String value) {
		return new RequestAction<Boolean>()
				.request(new SetBit(key, offset, value))
				.connection(connectionPool.getConnection())
				.responseCast(BOOLEAN)
				.execute()
				.get(timeout);
	}

	@Override
	public Boolean getbit(String key, long offset) {
		return new RequestAction<Boolean>()
				.request(new GetBit(key, offset))
				.connection(connectionPool.getConnection())
				.responseCast(BOOLEAN)
				.execute()
				.get(timeout);
	}

	@Override
	public Long setrange(String key, long offset, String value) {
		return new RequestAction<Long>()
				.request(new SetRange(key, offset, value))
				.connection(connectionPool.getConnection())
				.responseCast(LONG)
				.execute()
				.get(timeout);
	}

	@Override
	public String getrange(String key, long startOffset, long endOffset) {
		return new RequestAction<String>()
				.request(new GetRange(key, startOffset, endOffset))
				.connection(connectionPool.getConnection())
				.responseCast(STRING)
				.execute()
				.get(timeout);
	}

	@Override
	public String getSet(String key, String value) {
		return new RequestAction<String>()
				.request(new GetSet(key, value))
				.connection(connectionPool.getConnection())
				.responseCast(STRING)
				.execute()
				.get(timeout);
	}

	@Override
	public Long setnx(String key, String value) {
		return new RequestAction<Long>()
				.request(new SetNx(key, value))
				.connection(connectionPool.getConnection())
				.responseCast(LONG)
				.execute()
				.get(timeout);
	}

	@Override
	public String setex(String key, int seconds, String value) {
		return new RequestAction<String>()
				.request(new SetEx(key, seconds, value))
				.connection(connectionPool.getConnection())
				.responseCast(STRING)
				.execute()
				.get(timeout);
	}

	@Override
	public String psetex(String key, long milliseconds, String value) {
		return new RequestAction<String>()
				.request(new PSetEx(key, milliseconds, value))
				.connection(connectionPool.getConnection())
				.responseCast(STRING)
				.execute()
				.get(timeout);
	}

	@Override
	public Long decrBy(String key, long integer) {
		return new RequestAction<Long>()
				.request(new DecrementBy(key, integer))
				.connection(connectionPool.getConnection())
				.responseCast(LONG)
				.execute()
				.get(timeout);
	}

	@Override
	public Long decr(String key) {
		return new RequestAction<Long>()
				.request(new Decrement(key))
				.connection(connectionPool.getConnection())
				.responseCast(LONG)
				.execute()
				.get(timeout);
	}

	@Override
	public Long incrBy(String key, long integer) {
		return new RequestAction<Long>()
				.request(new IncrementBy(key, integer))
				.connection(connectionPool.getConnection())
				.responseCast(LONG)
				.execute()
				.get(timeout);
	}

	@Override
	public Double incrByFloat(String key, double value) {
		return new RequestAction<Double>()
				.request(new IncrementByFloat(key, value))
				.connection(connectionPool.getConnection())
				.responseCast(DOUBLE)
				.execute()
				.get(timeout);
	}

	@Override
	public Long incr(String key) {
		return new RequestAction<Long>()
				.request(new Increment(key))
				.connection(connectionPool.getConnection())
				.responseCast(LONG)
				.execute()
				.get(timeout);
	}

	@Override
	public Long append(String key, String value) {
		return new RequestAction<Long>()
				.request(new Append(key, value))
				.connection(connectionPool.getConnection())
				.responseCast(LONG)
				.execute()
				.get(timeout);
	}

	@Override
	public String substr(String key, int start, int end) {
		return new RequestAction<String>()
				.request(new SubStr(key, start, end))
				.connection(connectionPool.getConnection())
				.responseCast(STRING)
				.execute()
				.get(timeout);
	}

	@Override
	public Long hset(String key, String field, String value) {
		return new RequestAction<Long>()
				.request(new HSet(key, field, value))
				.connection(connectionPool.getConnection())
				.responseCast(LONG)
				.execute()
				.get(timeout);
	}

	@Override
	public String hget(String key, String field) {
		return new RequestAction<String>()
				.request(new HGet(key, field))
				.connection(connectionPool.getConnection())
				.responseCast(STRING)
				.execute()
				.get(timeout);
	}

	@Override
	public Long hsetnx(String key, String field, String value) {
		return null;
	}

	@Override
	public String hmset(String key, Map<String, String> hash) {
		return null;
	}

	@Override
	public List<String> hmget(String key, String... fields) {
		return null;
	}

	@Override
	public Long hincrBy(String key, String field, long value) {
		return null;
	}

	@Override
	public Double hincrByFloat(String key, String field, double value) {
		return null;
	}

	@Override
	public Boolean hexists(String key, String field) {
		return null;
	}

	@Override
	public Long hdel(String key, String... field) {
		return null;
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
		return null;
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
		return null;
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
