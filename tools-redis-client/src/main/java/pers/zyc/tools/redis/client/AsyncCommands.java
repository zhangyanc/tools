package pers.zyc.tools.redis.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhangyancheng
 */
public interface AsyncCommands {

	ResponseFuture<String> _set(String key, String value);

	ResponseFuture<String> _set(String key, String value, String nxxx, String expx, long time);

	ResponseFuture<String> _set(String key, String value, String nxxx);

	ResponseFuture<String> _get(String key);

	ResponseFuture<Boolean> _exists(String key);

	ResponseFuture<Long> _persist(String key);

	ResponseFuture<String> _type(String key);

	ResponseFuture<Long> _expire(String key, int seconds);

	ResponseFuture<Long> _pexpire(String key, long milliseconds);

	ResponseFuture<Long> _expireAt(String key, long unixTime);

	ResponseFuture<Long> _pexpireAt(String key, long millisecondsTimestamp);

	ResponseFuture<Long> _ttl(String key);

	ResponseFuture<Long> _pttl(String key);

	ResponseFuture<Boolean> _setbit(String key, long offset, boolean value);

	ResponseFuture<Boolean> _setbit(String key, long offset, String value);

	ResponseFuture<Boolean> _getbit(String key, long offset);

	ResponseFuture<Long> _setrange(String key, long offset, String value);

	ResponseFuture<String> _getrange(String key, long startOffset, long endOffset);

	ResponseFuture<String> _getSet(String key, String value);

	ResponseFuture<Long> _setnx(String key, String value);

	ResponseFuture<String> _setex(String key, int seconds, String value);

	ResponseFuture<String> _psetex(String key, long milliseconds, String value);

	ResponseFuture<Long> _decrBy(String key, long integer);

	ResponseFuture<Long> _decr(String key);

	ResponseFuture<Long> _incrBy(String key, long integer);

	ResponseFuture<Double> _incrByFloat(String key, double value);

	ResponseFuture<Long> _incr(String key);

	ResponseFuture<Long> _append(String key, String value);

	ResponseFuture<String> _substr(String key, int start, int end);

	ResponseFuture<Long> _hset(String key, String field, String value);

	ResponseFuture<String> _hget(String key, String field);

	ResponseFuture<Long> _hsetnx(String key, String field, String value);

	ResponseFuture<String> _hmset(String key, Map<String, String> _hash);

	ResponseFuture<List<String>> _hmget(String key, String... fields);

	ResponseFuture<Long> _hincrBy(String key, String field, long value);

	ResponseFuture<Double> _hincrByFloat(String key, String field, double value);

	ResponseFuture<Boolean> _hexists(String key, String field);

	ResponseFuture<Long> _hdel(String key, String... field);

	ResponseFuture<Long> _hlen(String key);

	ResponseFuture<Set<String>> _hkeys(String key);

	ResponseFuture<List<String>> _hvals(String key);

	ResponseFuture<Map<String, String>> _hgetAll(String key);

	ResponseFuture<Long> _rpush(String key, String... string);

	ResponseFuture<Long> _lpush(String key, String... string);

	ResponseFuture<Long> _llen(String key);

	ResponseFuture<List<String>> _lrange(String key, long start, long end);

	ResponseFuture<String> _ltrim(String key, long start, long end);

	ResponseFuture<String> _lindex(String key, long index);

	ResponseFuture<String> _lset(String key, long index, String value);

	ResponseFuture<Long> _lrem(String key, long count, String value);

	ResponseFuture<String> _lpop(String key);

	ResponseFuture<String> _rpop(String key);

	ResponseFuture<Long> _sadd(String key, String... member);

	ResponseFuture<Set<String>> _smembers(String key);

	ResponseFuture<Long> _srem(String key, String... member);

	ResponseFuture<String> _spop(String key);

	ResponseFuture<Set<String>> _spop(String key, long count);

	ResponseFuture<Long> _scard(String key);

	ResponseFuture<Boolean> _sismember(String key, String member);

	ResponseFuture<String> _srandmember(String key);

	ResponseFuture<List<String>> _srandmember(String key, int count);

	ResponseFuture<Long> _strlen(String key);

	ResponseFuture<Long> _zadd(String key, double score, String member);

	//Long zadd(String key, double score, String member, ZAddParams params);

	ResponseFuture<Long> _zadd(String key, Map<String, Double> _scoreMembers);

	//Long zadd(String key, Map<String, Double> _scoreMembers, ZAddParams params);

	ResponseFuture<Set<String>> _zrange(String key, long start, long end);

	ResponseFuture<Long> _zrem(String key, String... member);

	ResponseFuture<Double> _zincrby(String key, double score, String member);

	//Double zincrby(String key, double score, String member, ZIncrByParams params);

	ResponseFuture<Long> _zrank(String key, String member);

	ResponseFuture<Long> _zrevrank(String key, String member);

	ResponseFuture<Set<String>> _zrevrange(String key, long start, long end);

	//Set<Tuple> _zrangeWithScores(String key, long start, long end);

	//Set<Tuple> _zrevrangeWithScores(String key, long start, long end);

	ResponseFuture<Long> _zcard(String key);

	ResponseFuture<Double> _zscore(String key, String member);

	ResponseFuture<List<String>> _sort(String key);

	//List<String> _sort(String key, SortingParams sortingParameters);

	ResponseFuture<Long> _zcount(String key, double min, double max);

	ResponseFuture<Long> _zcount(String key, String min, String max);

	ResponseFuture<Set<String>> _zrangeByScore(String key, double min, double max);

	ResponseFuture<Set<String>> _zrangeByScore(String key, String min, String max);

	ResponseFuture<Set<String>> _zrevrangeByScore(String key, double max, double min);

	ResponseFuture<Set<String>> _zrangeByScore(String key, double min, double max, int offset, int count);

	ResponseFuture<Set<String>> _zrevrangeByScore(String key, String max, String min);

	ResponseFuture<Set<String>> _zrangeByScore(String key, String min, String max, int offset, int count);

	ResponseFuture<Set<String>> _zrevrangeByScore(String key, double max, double min, int offset, int count);

	//Set<Tuple> _zrangeByScoreWithScores(String key, double min, double max);

	//Set<Tuple> _zrevrangeByScoreWithScores(String key, double max, double min);

	//Set<Tuple> _zrangeByScoreWithScores(String key, double min, double max, int offset, int count);

	ResponseFuture<Set<String>> _zrevrangeByScore(String key, String max, String min, int offset, int count);

	//Set<Tuple> _zrangeByScoreWithScores(String key, String min, String max);

	//Set<Tuple> _zrevrangeByScoreWithScores(String key, String max, String min);

	//Set<Tuple> _zrangeByScoreWithScores(String key, String min, String max, int offset, int count);

	//Set<Tuple> _zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count);

	//Set<Tuple> _zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count);

	ResponseFuture<Long> _zremrangeByRank(String key, long start, long end);

	ResponseFuture<Long> _zremrangeByScore(String key, double start, double end);

	ResponseFuture<Long> _zremrangeByScore(String key, String start, String end);

	ResponseFuture<Long> _zlexcount(String key, String min, String max);

	ResponseFuture<Set<String>> _zrangeByLex(String key, String min, String max);

	ResponseFuture<Set<String>> _zrangeByLex(String key, String min, String max, int offset, int count);

	ResponseFuture<Set<String>> _zrevrangeByLex(String key, String max, String min);

	ResponseFuture<Set<String>> _zrevrangeByLex(String key, String max, String min, int offset, int count);

	ResponseFuture<Long> _zremrangeByLex(String key, String min, String max);

	//Long linsert(String key, BinaryClient.LIST_POSITION where, String pivot, String value);

	ResponseFuture<Long> _lpushx(String key, String... string);

	ResponseFuture<Long> _rpushx(String key, String... string);

	ResponseFuture<List<String>> _blpop(String arg);

	ResponseFuture<List<String>> _blpop(int timeout, String key);

	ResponseFuture<List<String>> _brpop(String arg);

	ResponseFuture<List<String>> _brpop(int timeout, String key);

	ResponseFuture<Long> _del(String key);

	ResponseFuture<String> _echo(String string);

	ResponseFuture<Long> _move(String key, int dbIndex);

	ResponseFuture<Long> _bitcount(String key);

	ResponseFuture<Long> _bitcount(String key, long start, long end);

	ResponseFuture<Long> _bitpos(String key, boolean value);

	//Long bitpos(String key, boolean value, BitPosParams params);

	//ScanResult<Map.Entry<String, String>> _hscan(String key, int cursor);

	//ScanResult<String> _sscan(String key, int cursor);

	//ScanResult<Tuple> _zscan(String key, int cursor);

	//ScanResult<Map.Entry<String, String>> _hscan(String key, String cursor);

	//ScanResult<Map.Entry<String, String>> _hscan(String key, String cursor, ScanParams params);

	//ScanResult<String> _sscan(String key, String cursor);

	//ScanResult<String> _sscan(String key, String cursor, ScanParams params);

	//ScanResult<Tuple> _zscan(String key, String cursor);

	//ScanResult<Tuple> _zscan(String key, String cursor, ScanParams params);

	ResponseFuture<Long> _pfadd(String key, String... elements);

	ResponseFuture<Long> _pfcount(String key);

	ResponseFuture<Long> _geoadd(String key, double longitude, double latitude, String member);

	//Long geoadd(String key, Map<String, GeoCoordinate> _memberCoordinateMap);

	ResponseFuture<Double> _geodist(String key, String member1, String member2);

	//Double geodist(String key, String member1, String member2, GeoUnit unit);

	ResponseFuture<List<String>> _geohash(String key, String... members);

	//List<GeoCoordinate> _geopos(String key, String... members);

	//List<GeoRadiusResponse> _georadius(String key, double longitude, double latitude, double radius, GeoUnit unit);

	//List<GeoRadiusResponse> _georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param);

	//List<GeoRadiusResponse> _georadiusByMember(String key, String member, double radius, GeoUnit unit);

	//List<GeoRadiusResponse> _georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param);

	ResponseFuture<List<Long>> _bitfield(String key, String... arguments);
}
