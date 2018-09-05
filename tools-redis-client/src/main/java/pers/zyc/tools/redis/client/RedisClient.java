package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.request.KeyType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhangyancheng
 */
public class RedisClient extends AsyncClient implements SyncCommands {

	public RedisClient(ClientConfig config) {
		super(config);
	}

	@Override
	public void set(String key, String value) {
		_set(key, value)
				.get();
	}

	@Override
	public boolean set(String key, String value, String nxxx, String expx, long time) {
		return _set(key, value, nxxx, expx, time)
				.get();
	}

	@Override
	public boolean set(String key, String value, String nxxx) {
		return _set(key, value, nxxx)
				.get();
	}

	@Override
	public String get(String key) {
		return _get(key)
				.get();
	}

	@Override
	public boolean exists(String key) {
		return _exists(key)
				.get();
	}

	@Override
	public boolean persist(String key) {
		return _persist(key)
				.get();
	}

	@Override
	public KeyType type(String key) {
		return _type(key)
				.get();
	}

	@Override
	public boolean expire(String key, int seconds) {
		return _expire(key, seconds)
				.get();
	}

	@Override
	public boolean pexpire(String key, long milliseconds) {
		return _pexpire(key, milliseconds)
				.get();
	}

	@Override
	public boolean expireAt(String key, long unixTime) {
		return _expireAt(key, unixTime)
				.get();
	}

	@Override
	public boolean pexpireAt(String key, long millisecondsTimestamp) {
		return _pexpireAt(key, millisecondsTimestamp)
				.get();
	}

	@Override
	public long ttl(String key) {
		return _ttl(key)
				.get();
	}

	@Override
	public long pttl(String key) {
		return _pttl(key)
				.get();
	}

	@Override
	public boolean setbit(String key, long offset, boolean value) {
		return _setbit(key, offset, value)
				.get();
	}

	@Override
	public boolean setbit(String key, long offset, String value) {
		return _setbit(key, offset, value)
				.get();
	}

	@Override
	public boolean getbit(String key, long offset) {
		return _getbit(key, offset)
				.get();
	}

	@Override
	public long setrange(String key, long offset, String value) {
		return _setrange(key, offset, value)
				.get();
	}

	@Override
	public String getrange(String key, long startOffset, long endOffset) {
		return _getrange(key, startOffset, endOffset)
				.get();
	}

	@Override
	public String getSet(String key, String value) {
		return _getSet(key, value)
				.get();
	}

	@Override
	public boolean setnx(String key, String value) {
		return _setnx(key, value)
				.get();
	}

	@Override
	public void setex(String key, int seconds, String value) {
		_setex(key, seconds, value)
				.get();
	}

	@Override
	public void psetex(String key, long milliseconds, String value) {
		_psetex(key, milliseconds, value)
				.get();
	}

	@Override
	public long decrBy(String key, long integer) {
		return _decrBy(key, integer)
				.get();
	}

	@Override
	public long decr(String key) {
		return _decr(key)
				.get();
	}

	@Override
	public long incrBy(String key, long integer) {
		return _incrBy(key, integer)
				.get();
	}

	@Override
	public double incrByFloat(String key, double value) {
		return _incrByFloat(key, value)
				.get();
	}

	@Override
	public long incr(String key) {
		return _incr(key)
				.get();
	}

	@Override
	public long append(String key, String value) {
		return _append(key, value)
				.get();
	}

	@Override
	public String substr(String key, int start, int end) {
		return _substr(key, start, end)
				.get();
	}

	@Override
	public boolean hset(String key, String field, String value) {
		return _hset(key, field, value)
				.get();
	}

	@Override
	public String hget(String key, String field) {
		return _hget(key, field)
				.get();
	}

	@Override
	public boolean hsetnx(String key, String field, String value) {
		return _hsetnx(key, field, value)
				.get();
	}

	@Override
	public void hmset(String key, Map<String, String> hash) {
		_hmset(key, hash)
				.get();
	}

	@Override
	public List<String> hmget(String key, String... fields) {
		return _hmget(key, fields)
				.get();
	}

	@Override
	public long hincrBy(String key, String field, long value) {
		return _hincrBy(key, field, value)
				.get();
	}

	@Override
	public double hincrByFloat(String key, String field, double value) {
		return _hincrByFloat(key, field, value)
				.get();
	}

	@Override
	public boolean hexists(String key, String field) {
		return _hexists(key, field)
				.get();
	}

	@Override
	public long hdel(String key, String... field) {
		return _hdel(key, field)
				.get();
	}

	@Override
	public Long hlen(String key) {
		return _hlen(key)
				.get();
	}

	@Override
	public Set<String> hkeys(String key) {
		return _hkeys(key)
				.get();
	}

	@Override
	public List<String> hvals(String key) {
		return _hvals(key)
				.get();
	}

	@Override
	public Map<String, String> hgetAll(String key) {
		return _hgetAll(key)
				.get();
	}

	@Override
	public Long rpush(String key, String... string) {
		return _rpush(key, string)
				.get();
	}

	@Override
	public Long lpush(String key, String... string) {
		return _lpush(key, string)
				.get();
	}

	@Override
	public Long llen(String key) {
		return _llen(key)
				.get();
	}

	@Override
	public List<String> lrange(String key, long start, long end) {
		return _lrange(key, start, end)
				.get();
	}

	@Override
	public String ltrim(String key, long start, long end) {
		return _ltrim(key, start, end)
				.get();
	}

	@Override
	public String lindex(String key, long index) {
		return _lindex(key, index)
				.get();
	}

	@Override
	public String lset(String key, long index, String value) {
		return _lset(key, index, value)
				.get();
	}

	@Override
	public Long lrem(String key, long count, String value) {
		return _lrem(key, count, value)
				.get();
	}

	@Override
	public String lpop(String key) {
		return _lpop(key)
				.get();
	}

	@Override
	public String rpop(String key) {
		return _rpop(key)
				.get();
	}

	@Override
	public Long sadd(String key, String... member) {
		return _sadd(key, member)
				.get();
	}

	@Override
	public Set<String> smembers(String key) {
		return _smembers(key)
				.get();
	}
	//TODO
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
	public long strlen(String key) {
		return _strlen(key)
				.get();
	}

	@Override
	public Long zadd(String key, double score, String member) {
		return null;
	}

	@Override
	public Long zadd(String key, Map<String, Double> scoreMembers) {
		return null;
	}

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

	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
		return null;
	}

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
	public long del(String key) {
		return _del(key).get();
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

	@Override
	public Double geodist(String key, String member1, String member2) {
		return null;
	}

	@Override
	public List<String> geohash(String key, String... members) {
		return null;
	}

	@Override
	public List<Long> bitfield(String key, String... arguments) {
		return null;
	}
}
