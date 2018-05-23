package pers.zyc.tools.cacheclient.redis;

import redis.clients.jedis.*;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhangyancheng
 */
public interface RedisCacheClient extends JedisCommands {

	/**
	 * 将字符串value设置到key, 如果key已存在则覆盖就值, 且无视类型
	 *
	 * @param key key
	 * @param value value(不能超过一个GB)
	 * @return 响应状态码
	 */
	@Override
	String set(String key, String value);

	/**
	 * 将字符串value设置到key, 并且设置过期时间,
	 *
	 * @param key key
	 * @param value value(不能超过一个GB)
	 * @param nxxx 可选值为"NX"或者XX
	 *             NX -- 当且仅当key不存在才设置
	 *             XX -- 当前仅当key存在才设置
	 * @param expx 过期时间单位,可选值为"EX"或者"PX"
	 *             EX -- 秒
	 *             PX --毫秒
	 * @param time 过期时间
	 * @return 响应状态码
	 */
	@Override
	String set(String key, String value, String nxxx, String expx, long time);

	/**
	 * 将字符串value设置到key
	 *
	 * @param key key
	 * @param value value(不能超过一个GB)
	 * @param nxxx 可选值为"NX"或者XX
	 *             NX -- 当且仅当key不存在才设置
	 *             XX -- 当前仅当key存在才设置
	 * @return 响应状态码
	 */
	@Override
	String set(String key, String value, String nxxx);

	/**
	 * 获取key设置的的字符串值
	 *
	 * @param key key
	 * @return 如果key不存在返回null, 如果key设置的非字符串类型将返回一个错误
	 */
	@Override
	String get(String key);

	/**
	 * 检查key是否存在
	 *
	 * @param key key
	 * @return key是否存在
	 */
	@Override
	Boolean exists(String key);

	/**
	 * 取消{@link #expire(String, int) expire}设置在key上的超时时间
	 *
	 * @param key key
	 * @return 如果key设置了过期时间返回1, 如果不存在或者未设置过期时间返回0
	 */
	@Override
	Long persist(String key);

	/**
	 *
	 * @param key key
	 * @return
	 */
	@Override
	String type(String key);

	@Override
	Long expire(String key, int seconds);

	@Override
	Long pexpire(String key, long milliseconds);

	@Override
	Long expireAt(String key, long unixTime);

	@Override
	Long pexpireAt(String key, long millisecondsTimestamp);

	@Override
	Long ttl(String key);

	@Override
	Long pttl(String key);

	@Override
	Boolean setbit(String key, long offset, boolean value);

	@Override
	Boolean setbit(String key, long offset, String value);

	@Override
	Boolean getbit(String key, long offset);

	@Override
	Long setrange(String key, long offset, String value);

	@Override
	String getrange(String key, long startOffset, long endOffset);

	@Override
	String getSet(String key, String value);

	/**
	 * 将字符串value设置到key, 当且仅当key不存在才设置成功
	 *
	 * @param key key
	 * @param value value(不能超过一个GB)
	 * @return 1 -- 设置成功, 0 -- 设置未成功
	 */
	@Override
	Long setnx(String key, String value);

	@Override
	String setex(String key, int seconds, String value);

	@Override
	String psetex(String key, long milliseconds, String value);

	@Override
	Long decrBy(String key, long integer);

	@Override
	Long decr(String key);

	@Override
	Long incrBy(String key, long integer);

	@Override
	Double incrByFloat(String key, double value);

	@Override
	Long incr(String key);

	@Override
	Long append(String key, String value);

	@Override
	String substr(String key, int start, int end);

	@Override
	Long hset(String key, String field, String value);

	@Override
	String hget(String key, String field);

	@Override
	Long hsetnx(String key, String field, String value);

	@Override
	String hmset(String key, Map<String, String> hash);

	@Override
	List<String> hmget(String key, String... fields);

	@Override
	Long hincrBy(String key, String field, long value);

	@Override
	Double hincrByFloat(String key, String field, double value);

	@Override
	Boolean hexists(String key, String field);

	@Override
	Long hdel(String key, String... field);

	@Override
	Long hlen(String key);

	@Override
	Set<String> hkeys(String key);

	@Override
	List<String> hvals(String key);

	@Override
	Map<String, String> hgetAll(String key);

	@Override
	Long rpush(String key, String... string);

	@Override
	Long lpush(String key, String... string);

	@Override
	Long llen(String key);

	@Override
	List<String> lrange(String key, long start, long end);

	@Override
	String ltrim(String key, long start, long end);

	@Override
	String lindex(String key, long index);

	@Override
	String lset(String key, long index, String value);

	@Override
	Long lrem(String key, long count, String value);

	@Override
	String lpop(String key);

	@Override
	String rpop(String key);

	@Override
	Long sadd(String key, String... member);

	@Override
	Set<String> smembers(String key);

	@Override
	Long srem(String key, String... member);

	@Override
	String spop(String key);

	@Override
	Set<String> spop(String key, long count);

	@Override
	Long scard(String key);

	@Override
	Boolean sismember(String key, String member);

	@Override
	String srandmember(String key);

	@Override
	List<String> srandmember(String key, int count);

	@Override
	Long strlen(String key);

	@Override
	Long zadd(String key, double score, String member);

	@Override
	Long zadd(String key, double score, String member, ZAddParams params);

	@Override
	Long zadd(String key, Map<String, Double> scoreMembers);

	@Override
	Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params);

	@Override
	Set<String> zrange(String key, long start, long end);

	@Override
	Long zrem(String key, String... member);

	@Override
	Double zincrby(String key, double score, String member);

	@Override
	Double zincrby(String key, double score, String member, ZIncrByParams params);

	@Override
	Long zrank(String key, String member);

	@Override
	Long zrevrank(String key, String member);

	@Override
	Set<String> zrevrange(String key, long start, long end);

	@Override
	Set<Tuple> zrangeWithScores(String key, long start, long end);

	@Override
	Set<Tuple> zrevrangeWithScores(String key, long start, long end);

	@Override
	Long zcard(String key);

	@Override
	Double zscore(String key, String member);

	@Override
	List<String> sort(String key);

	@Override
	List<String> sort(String key, SortingParams sortingParameters);

	@Override
	Long zcount(String key, double min, double max);

	@Override
	Long zcount(String key, String min, String max);

	@Override
	Set<String> zrangeByScore(String key, double min, double max);

	@Override
	Set<String> zrangeByScore(String key, String min, String max);

	@Override
	Set<String> zrevrangeByScore(String key, double max, double min);

	@Override
	Set<String> zrangeByScore(String key, double min, double max, int offset, int count);

	@Override
	Set<String> zrevrangeByScore(String key, String max, String min);

	@Override
	Set<String> zrangeByScore(String key, String min, String max, int offset, int count);

	@Override
	Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count);

	@Override
	Set<Tuple> zrangeByScoreWithScores(String key, double min, double max);

	@Override
	Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min);

	@Override
	Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count);

	@Override
	Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count);

	@Override
	Set<Tuple> zrangeByScoreWithScores(String key, String min, String max);

	@Override
	Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min);

	@Override
	Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count);

	@Override
	Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count);

	@Override
	Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count);

	@Override
	Long zremrangeByRank(String key, long start, long end);

	@Override
	Long zremrangeByScore(String key, double start, double end);

	@Override
	Long zremrangeByScore(String key, String start, String end);

	@Override
	Long zlexcount(String key, String min, String max);

	@Override
	Set<String> zrangeByLex(String key, String min, String max);

	@Override
	Set<String> zrangeByLex(String key, String min, String max, int offset, int count);

	@Override
	Set<String> zrevrangeByLex(String key, String max, String min);

	@Override
	Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count);

	@Override
	Long zremrangeByLex(String key, String min, String max);

	@Override
	Long linsert(String key, BinaryClient.LIST_POSITION where, String pivot, String value);

	@Override
	Long lpushx(String key, String... string);

	@Override
	Long rpushx(String key, String... string);

	@Override
	List<String> blpop(String arg);

	@Override
	List<String> blpop(int timeout, String key);

	@Override
	List<String> brpop(String arg);

	@Override
	List<String> brpop(int timeout, String key);

	@Override
	Long del(String key);

	@Override
	String echo(String string);

	@Override
	Long move(String key, int dbIndex);

	@Override
	Long bitcount(String key);

	@Override
	Long bitcount(String key, long start, long end);

	@Override
	Long bitpos(String key, boolean value);

	@Override
	Long bitpos(String key, boolean value, BitPosParams params);

	@Override
	ScanResult<Map.Entry<String, String>> hscan(String key, int cursor);

	@Override
	ScanResult<String> sscan(String key, int cursor);

	@Override
	ScanResult<Tuple> zscan(String key, int cursor);

	@Override
	ScanResult<Map.Entry<String, String>> hscan(String key, String cursor);

	@Override
	ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams params);

	@Override
	ScanResult<String> sscan(String key, String cursor);

	@Override
	ScanResult<String> sscan(String key, String cursor, ScanParams params);

	@Override
	ScanResult<Tuple> zscan(String key, String cursor);

	@Override
	ScanResult<Tuple> zscan(String key, String cursor, ScanParams params);

	@Override
	Long pfadd(String key, String... elements);

	@Override
	long pfcount(String key);

	@Override
	Long geoadd(String key, double longitude, double latitude, String member);

	@Override
	Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap);

	@Override
	Double geodist(String key, String member1, String member2);

	@Override
	Double geodist(String key, String member1, String member2, GeoUnit unit);

	@Override
	List<String> geohash(String key, String... members);

	@Override
	List<GeoCoordinate> geopos(String key, String... members);

	@Override
	List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit);

	@Override
	List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param);

	@Override
	List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit);

	@Override
	List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param);

	@Override
	List<Long> bitfield(String key, String... arguments);
}
