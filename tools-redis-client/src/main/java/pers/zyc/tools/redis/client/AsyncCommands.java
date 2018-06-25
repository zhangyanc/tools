package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.util.Future;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhangyancheng
 */
public interface AsyncCommands {

	Future<String> _set(String key, String value);

	Future<String> _set(String key, String value, String nxxx, String expx, long time);

	Future<String> _set(String key, String value, String nxxx);

	Future<String> _get(String key);

	Future<Boolean> _exists(String key);

	Future<Long> _persist(String key);

	Future<String> _type(String key);

	Future<Long> _expire(String key, int seconds);

	Future<Long> _pexpire(String key, long milliseconds);

	Future<Long> _expireAt(String key, long unixTime);

	Future<Long> _pexpireAt(String key, long millisecondsTimestamp);

	Future<Long> _ttl(String key);

	Future<Long> _pttl(String key);

	Future<Boolean> _setbit(String key, long offset, boolean value);

	Future<Boolean> _setbit(String key, long offset, String value);

	Future<Boolean> _getbit(String key, long offset);

	Future<Long> _setrange(String key, long offset, String value);

	Future<String> _getrange(String key, long startOffset, long endOffset);

	Future<String> _getSet(String key, String value);

	Future<Long> _setnx(String key, String value);

	Future<String> _setex(String key, int seconds, String value);

	Future<String> _psetex(String key, long milliseconds, String value);

	Future<Long> _decrBy(String key, long integer);

	Future<Long> _decr(String key);

	Future<Long> _incrBy(String key, long integer);

	Future<Double> _incrByFloat(String key, double value);

	Future<Long> _incr(String key);

	Future<Long> _append(String key, String value);

	Future<String> _substr(String key, int start, int end);

	Future<Long> _hset(String key, String field, String value);

	Future<String> _hget(String key, String field);

	Future<Long> _hsetnx(String key, String field, String value);

	Future<String> _hmset(String key, Map<String, String> _hash);

	Future<List<String>> _hmget(String key, String... fields);

	Future<Long> _hincrBy(String key, String field, long value);

	Future<Double> _hincrByFloat(String key, String field, double value);

	Future<Boolean> _hexists(String key, String field);

	Future<Long> _hdel(String key, String... field);

	Future<Long> _hlen(String key);

	Future<Set<String>> _hkeys(String key);

	Future<List<String>> _hvals(String key);

	Future<Map<String, String>> _hgetAll(String key);

	Future<Long> _rpush(String key, String... string);

	Future<Long> _lpush(String key, String... string);

	Future<Long> _llen(String key);

	Future<List<String>> _lrange(String key, long start, long end);

	Future<String> _ltrim(String key, long start, long end);

	Future<String> _lindex(String key, long index);

	Future<String> _lset(String key, long index, String value);

	Future<Long> _lrem(String key, long count, String value);

	Future<String> _lpop(String key);

	Future<String> _rpop(String key);

	Future<Long> _sadd(String key, String... member);

	Future<Set<String>> _smembers(String key);

	Future<Long> _srem(String key, String... member);

	Future<String> _spop(String key);

	Future<Set<String>> _spop(String key, long count);

	Future<Long> _scard(String key);

	Future<Boolean> _sismember(String key, String member);

	Future<String> _srandmember(String key);

	Future<List<String>> _srandmember(String key, int count);

	Future<Long> _strlen(String key);

	Future<Long> _zadd(String key, double score, String member);

	//Long zadd(String key, double score, String member, ZAddParams params);

	Future<Long> _zadd(String key, Map<String, Double> _scoreMembers);

	//Long zadd(String key, Map<String, Double> _scoreMembers, ZAddParams params);

	Future<Set<String>> _zrange(String key, long start, long end);

	Future<Long> _zrem(String key, String... member);

	Future<Double> _zincrby(String key, double score, String member);

	//Double zincrby(String key, double score, String member, ZIncrByParams params);

	Future<Long> _zrank(String key, String member);

	Future<Long> _zrevrank(String key, String member);

	Future<Set<String>> _zrevrange(String key, long start, long end);

	//Set<Tuple> _zrangeWithScores(String key, long start, long end);

	//Set<Tuple> _zrevrangeWithScores(String key, long start, long end);

	Future<Long> _zcard(String key);

	Future<Double> _zscore(String key, String member);

	Future<List<String>> _sort(String key);

	//List<String> _sort(String key, SortingParams sortingParameters);

	Future<Long> _zcount(String key, double min, double max);

	Future<Long> _zcount(String key, String min, String max);

	Future<Set<String>> _zrangeByScore(String key, double min, double max);

	Future<Set<String>> _zrangeByScore(String key, String min, String max);

	Future<Set<String>> _zrevrangeByScore(String key, double max, double min);

	Future<Set<String>> _zrangeByScore(String key, double min, double max, int offset, int count);

	Future<Set<String>> _zrevrangeByScore(String key, String max, String min);

	Future<Set<String>> _zrangeByScore(String key, String min, String max, int offset, int count);

	Future<Set<String>> _zrevrangeByScore(String key, double max, double min, int offset, int count);

	//Set<Tuple> _zrangeByScoreWithScores(String key, double min, double max);

	//Set<Tuple> _zrevrangeByScoreWithScores(String key, double max, double min);

	//Set<Tuple> _zrangeByScoreWithScores(String key, double min, double max, int offset, int count);

	Future<Set<String>> _zrevrangeByScore(String key, String max, String min, int offset, int count);

	//Set<Tuple> _zrangeByScoreWithScores(String key, String min, String max);

	//Set<Tuple> _zrevrangeByScoreWithScores(String key, String max, String min);

	//Set<Tuple> _zrangeByScoreWithScores(String key, String min, String max, int offset, int count);

	//Set<Tuple> _zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count);

	//Set<Tuple> _zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count);

	Future<Long> _zremrangeByRank(String key, long start, long end);

	Future<Long> _zremrangeByScore(String key, double start, double end);

	Future<Long> _zremrangeByScore(String key, String start, String end);

	Future<Long> _zlexcount(String key, String min, String max);

	Future<Set<String>> _zrangeByLex(String key, String min, String max);

	Future<Set<String>> _zrangeByLex(String key, String min, String max, int offset, int count);

	Future<Set<String>> _zrevrangeByLex(String key, String max, String min);

	Future<Set<String>> _zrevrangeByLex(String key, String max, String min, int offset, int count);

	Future<Long> _zremrangeByLex(String key, String min, String max);

	//Long linsert(String key, BinaryClient.LIST_POSITION where, String pivot, String value);

	Future<Long> _lpushx(String key, String... string);

	Future<Long> _rpushx(String key, String... string);

	Future<List<String>> _blpop(String arg);

	Future<List<String>> _blpop(int timeout, String key);

	Future<List<String>> _brpop(String arg);

	Future<List<String>> _brpop(int timeout, String key);

	Future<Long> _del(String key);

	Future<String> _echo(String string);

	Future<Long> _move(String key, int dbIndex);

	Future<Long> _bitcount(String key);

	Future<Long> _bitcount(String key, long start, long end);

	Future<Long> _bitpos(String key, boolean value);

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

	Future<Long> _pfadd(String key, String... elements);

	Future<Long> _pfcount(String key);

	Future<Long> _geoadd(String key, double longitude, double latitude, String member);

	//Long geoadd(String key, Map<String, GeoCoordinate> _memberCoordinateMap);

	Future<Double> _geodist(String key, String member1, String member2);

	//Double geodist(String key, String member1, String member2, GeoUnit unit);

	Future<List<String>> _geohash(String key, String... members);

	//List<GeoCoordinate> _geopos(String key, String... members);

	//List<GeoRadiusResponse> _georadius(String key, double longitude, double latitude, double radius, GeoUnit unit);

	//List<GeoRadiusResponse> _georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param);

	//List<GeoRadiusResponse> _georadiusByMember(String key, String member, double radius, GeoUnit unit);

	//List<GeoRadiusResponse> _georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param);

	Future<List<Long>> _bitfield(String key, String... arguments);
}
