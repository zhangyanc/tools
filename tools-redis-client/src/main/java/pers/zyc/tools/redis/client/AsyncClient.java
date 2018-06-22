package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.request.*;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static pers.zyc.tools.redis.client.ResponseCast.*;

/**
 * @author zhangyancheng
 */
public class AsyncClient implements AsyncCommands, Closeable {
	public static final int DEFAULT_CONNECTION_TIMEOUT = -1;
	public static final int DEFAULT_REQUEST_TIMEOUT = -1;
	public static final int DEFAULT_NET_WORKERS = 1;

	private final ConnectionPool connectionPool;

	public AsyncClient(ClientConfig config) {
		connectionPool = new ConnectionPool(config);
	}

	@Override
	public void close() {
		connectionPool.close();
	}

	@Override
	public Future<String> _set(String key, String value) {
		return connectionPool.getConnection().send(new pers.zyc.tools.redis.client.request.Set(key, value), STRING);
	}

	@Override
	public Future<String> _set(String key, String value, String nxxx, String expx, long time) {
		return connectionPool.getConnection().send(new pers.zyc.tools.redis.client.request.Set(key, value, nxxx, expx, time), STRING);
	}

	@Override
	public Future<String> _set(String key, String value, String nxxx) {
		return connectionPool.getConnection().send(new pers.zyc.tools.redis.client.request.Set(key, value, nxxx), STRING);
	}

	@Override
	public Future<String> _get(String key) {
		return connectionPool.getConnection().send(new Get(key), STRING);
	}

	@Override
	public Future<Boolean> _exists(String key) {
		return connectionPool.getConnection().send(new Exists(key), BOOLEAN);
	}

	@Override
	public Future<Long> _persist(String key) {
		return connectionPool.getConnection().send(new Persist(key), LONG);
	}

	@Override
	public Future<String> _type(String key) {
		return connectionPool.getConnection().send(new Type(key), STRING);
	}

	@Override
	public Future<Long> _expire(String key, int seconds) {
		return connectionPool.getConnection().send(new Expire(key, seconds), LONG);
	}

	@Override
	public Future<Long> _pexpire(String key, long milliseconds) {
		return connectionPool.getConnection().send(new PExpire(key, milliseconds), LONG);
	}

	@Override
	public Future<Long> _expireAt(String key, long unixTime) {
		return connectionPool.getConnection().send(new ExpireAt(key, unixTime), LONG);
	}

	@Override
	public Future<Long> _pexpireAt(String key, long millisecondsTimestamp) {
		return connectionPool.getConnection().send(new PExpireAt(key, millisecondsTimestamp), LONG);
	}

	@Override
	public Future<Long> _ttl(String key) {
		return connectionPool.getConnection().send(new Ttl(key), LONG);
	}

	@Override
	public Future<Long> _pttl(String key) {
		return connectionPool.getConnection().send(new PTtl(key), LONG);
	}

	@Override
	public Future<Boolean> _setbit(String key, long offset, boolean value) {
		return connectionPool.getConnection().send(new SetBit(key, offset, value), BOOLEAN);
	}

	@Override
	public Future<Boolean> _setbit(String key, long offset, String value) {
		return connectionPool.getConnection().send(new SetBit(key, offset, value), BOOLEAN);
	}

	@Override
	public Future<Boolean> _getbit(String key, long offset) {
		return connectionPool.getConnection().send(new GetBit(key, offset), BOOLEAN);
	}

	@Override
	public Future<Long> _setrange(String key, long offset, String value) {
		return connectionPool.getConnection().send(new SetRange(key, offset, value), LONG);
	}

	@Override
	public Future<String> _getrange(String key, long startOffset, long endOffset) {
		return connectionPool.getConnection().send(new GetRange(key, startOffset, endOffset), STRING);
	}

	@Override
	public Future<String> _getSet(String key, String value) {
		return connectionPool.getConnection().send(new GetSet(key, value), STRING);
	}

	@Override
	public Future<Long> _setnx(String key, String value) {
		return connectionPool.getConnection().send(new SetNx(key, value), LONG);
	}

	@Override
	public Future<String> _setex(String key, int seconds, String value) {
		return connectionPool.getConnection().send(new SetEx(key, seconds, value), STRING);
	}

	@Override
	public Future<String> _psetex(String key, long milliseconds, String value) {
		return connectionPool.getConnection().send(new PSetEx(key, milliseconds, value), STRING);
	}

	@Override
	public Future<Long> _decrBy(String key, long integer) {
		return connectionPool.getConnection().send(new DecrementBy(key, integer), LONG);
	}

	@Override
	public Future<Long> _decr(String key) {
		return connectionPool.getConnection().send(new Decrement(key), LONG);
	}

	@Override
	public Future<Long> _incrBy(String key, long integer) {
		return connectionPool.getConnection().send(new IncrementBy(key, integer), LONG);
	}

	@Override
	public Future<Double> _incrByFloat(String key, double value) {
		return connectionPool.getConnection().send(new IncrementByFloat(key, value), DOUBLE);
	}

	@Override
	public Future<Long> _incr(String key) {
		return connectionPool.getConnection().send(new Increment(key), LONG);
	}

	@Override
	public Future<Long> _append(String key, String value) {
		return connectionPool.getConnection().send(new Append(key, value), LONG);
	}

	@Override
	public Future<String> _substr(String key, int start, int end) {
		return connectionPool.getConnection().send(new SubStr(key, start, end), STRING);
	}

	@Override
	public Future<Long> _hset(String key, String field, String value) {
		return connectionPool.getConnection().send(new HSet(key, field, value), LONG);
	}

	@Override
	public Future<String> _hget(String key, String field) {
		return connectionPool.getConnection().send(new HGet(key, field), STRING);
	}

	@Override
	public Future<Long> _hsetnx(String key, String field, String value) {
		return connectionPool.getConnection().send(new HSetNx(key, field, value), LONG);
	}

	@Override
	public Future<String> _hmset(String key, Map<String, String> hash) {
		return connectionPool.getConnection().send(new HMSet(key, hash), STRING);
	}

	@Override
	public Future<List<String>> _hmget(String key, String... fields) {
		return connectionPool.getConnection().send(new HMGet(key, fields), STRING_LIST);
	}

	@Override
	public Future<Long> _hincrBy(String key, String field, long value) {
		return connectionPool.getConnection().send(new HIncrementBy(key, field, value), LONG);
	}

	@Override
	public Future<Double> _hincrByFloat(String key, String field, double value) {
		return connectionPool.getConnection().send(new HIncrementByFloat(key, field, value), DOUBLE);
	}

	@Override
	public Future<Boolean> _hexists(String key, String field) {
		return connectionPool.getConnection().send(new HExists(key, field), BOOLEAN);
	}

	@Override
	public Future<Long> _hdel(String key, String... field) {
		return connectionPool.getConnection().send(new HDelete(key, field), LONG);
	}

	@Override
	public Future<Long> _hlen(String key) {
		return null;
	}

	@Override
	public Future<Set<String>> _hkeys(String key) {
		return null;
	}

	@Override
	public Future<List<String>> _hvals(String key) {
		return null;
	}

	@Override
	public Future<Map<String, String>> _hgetAll(String key) {
		return null;
	}

	@Override
	public Future<Long> _rpush(String key, String... string) {
		return null;
	}

	@Override
	public Future<Long> _lpush(String key, String... string) {
		return null;
	}

	@Override
	public Future<Long> _llen(String key) {
		return null;
	}

	@Override
	public Future<List<String>> _lrange(String key, long start, long end) {
		return null;
	}

	@Override
	public Future<String> _ltrim(String key, long start, long end) {
		return null;
	}

	@Override
	public Future<String> _lindex(String key, long index) {
		return null;
	}

	@Override
	public Future<String> _lset(String key, long index, String value) {
		return null;
	}

	@Override
	public Future<Long> _lrem(String key, long count, String value) {
		return null;
	}

	@Override
	public Future<String> _lpop(String key) {
		return null;
	}

	@Override
	public Future<String> _rpop(String key) {
		return null;
	}

	@Override
	public Future<Long> _sadd(String key, String... member) {
		return null;
	}

	@Override
	public Future<Set<String>> _smembers(String key) {
		return null;
	}

	@Override
	public Future<Long> _srem(String key, String... member) {
		return null;
	}

	@Override
	public Future<String> _spop(String key) {
		return null;
	}

	@Override
	public Future<Set<String>> _spop(String key, long count) {
		return null;
	}

	@Override
	public Future<Long> _scard(String key) {
		return null;
	}

	@Override
	public Future<Boolean> _sismember(String key, String member) {
		return null;
	}

	@Override
	public Future<String> _srandmember(String key) {
		return null;
	}

	@Override
	public Future<List<String>> _srandmember(String key, int count) {
		return null;
	}

	@Override
	public Future<Long> _strlen(String key) {
		return connectionPool.getConnection().send(new StrLen(key), LONG);
	}

	@Override
	public Future<Long> _zadd(String key, double score, String member) {
		return null;
	}

	@Override
	public Future<Long> _zadd(String key, Map<String, Double> _scoreMembers) {
		return null;
	}

	@Override
	public Future<Set<String>> _zrange(String key, long start, long end) {
		return null;
	}

	@Override
	public Future<Long> _zrem(String key, String... member) {
		return null;
	}

	@Override
	public Future<Double> _zincrby(String key, double score, String member) {
		return null;
	}

	@Override
	public Future<Long> _zrank(String key, String member) {
		return null;
	}

	@Override
	public Future<Long> _zrevrank(String key, String member) {
		return null;
	}

	@Override
	public Future<Set<String>> _zrevrange(String key, long start, long end) {
		return null;
	}

	@Override
	public Future<Long> _zcard(String key) {
		return null;
	}

	@Override
	public Future<Double> _zscore(String key, String member) {
		return null;
	}

	@Override
	public Future<List<String>> _sort(String key) {
		return null;
	}

	@Override
	public Future<Long> _zcount(String key, double min, double max) {
		return null;
	}

	@Override
	public Future<Long> _zcount(String key, String min, String max) {
		return null;
	}

	@Override
	public Future<Set<String>> _zrangeByScore(String key, double min, double max) {
		return null;
	}

	@Override
	public Future<Set<String>> _zrangeByScore(String key, String min, String max) {
		return null;
	}

	@Override
	public Future<Set<String>> _zrevrangeByScore(String key, double max, double min) {
		return null;
	}

	@Override
	public Future<Set<String>> _zrangeByScore(String key, double min, double max, int offset, int count) {
		return null;
	}

	@Override
	public Future<Set<String>> _zrevrangeByScore(String key, String max, String min) {
		return null;
	}

	@Override
	public Future<Set<String>> _zrangeByScore(String key, String min, String max, int offset, int count) {
		return null;
	}

	@Override
	public Future<Set<String>> _zrevrangeByScore(String key, double max, double min, int offset, int count) {
		return null;
	}

	@Override
	public Future<Set<String>> _zrevrangeByScore(String key, String max, String min, int offset, int count) {
		return null;
	}

	@Override
	public Future<Long> _zremrangeByRank(String key, long start, long end) {
		return null;
	}

	@Override
	public Future<Long> _zremrangeByScore(String key, double start, double end) {
		return null;
	}

	@Override
	public Future<Long> _zremrangeByScore(String key, String start, String end) {
		return null;
	}

	@Override
	public Future<Long> _zlexcount(String key, String min, String max) {
		return null;
	}

	@Override
	public Future<Set<String>> _zrangeByLex(String key, String min, String max) {
		return null;
	}

	@Override
	public Future<Set<String>> _zrangeByLex(String key, String min, String max, int offset, int count) {
		return null;
	}

	@Override
	public Future<Set<String>> _zrevrangeByLex(String key, String max, String min) {
		return null;
	}

	@Override
	public Future<Set<String>> _zrevrangeByLex(String key, String max, String min, int offset, int count) {
		return null;
	}

	@Override
	public Future<Long> _zremrangeByLex(String key, String min, String max) {
		return null;
	}

	@Override
	public Future<Long> _lpushx(String key, String... string) {
		return null;
	}

	@Override
	public Future<Long> _rpushx(String key, String... string) {
		return null;
	}

	@Override
	public Future<List<String>> _blpop(String arg) {
		return null;
	}

	@Override
	public Future<List<String>> _blpop(int timeout, String key) {
		return null;
	}

	@Override
	public Future<List<String>> _brpop(String arg) {
		return null;
	}

	@Override
	public Future<List<String>> _brpop(int timeout, String key) {
		return null;
	}

	@Override
	public Future<Long> _del(String key) {
		return connectionPool.getConnection().send(new Delete(key), LONG);
	}

	@Override
	public Future<String> _echo(String string) {
		return null;
	}

	@Override
	public Future<Long> _move(String key, int dbIndex) {
		return null;
	}

	@Override
	public Future<Long> _bitcount(String key) {
		return null;
	}

	@Override
	public Future<Long> _bitcount(String key, long start, long end) {
		return null;
	}

	@Override
	public Future<Long> _bitpos(String key, boolean value) {
		return null;
	}

	@Override
	public Future<Long> _pfadd(String key, String... elements) {
		return null;
	}

	@Override
	public Future<Long> _pfcount(String key) {
		return null;
	}

	@Override
	public Future<Long> _geoadd(String key, double longitude, double latitude, String member) {
		return null;
	}

	@Override
	public Future<Double> _geodist(String key, String member1, String member2) {
		return null;
	}

	@Override
	public Future<List<String>> _geohash(String key, String... members) {
		return null;
	}

	@Override
	public Future<List<Long>> _bitfield(String key, String... arguments) {
		return null;
	}
}