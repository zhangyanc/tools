package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.request.hash.*;
import pers.zyc.tools.redis.client.request.key.*;
import pers.zyc.tools.redis.client.request.string.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhangyancheng
 */
public interface SyncCommands {

	/**
	 * @see pers.zyc.tools.redis.client.request.string.Set
	 */
	void set(String key, String value);

	/**
	 * @see pers.zyc.tools.redis.client.request.string.Set
	 */
	boolean set(String key, String value, String nxxx, String expx, long time);

	/**
	 * @see pers.zyc.tools.redis.client.request.string.Set
	 */
	boolean set(String key, String value, String nxxx);

	/**
	 * @see Get
	 */
	String get(String key);

	/**
	 * 检查键是否存在
	 *
	 * @param key 键
	 * @return 键是否存在
	 */
	boolean exists(String key);

	/**
	 * @see Persist
	 */
	boolean persist(String key);

	/**
	 * @see Type
	 */
	String type(String key);

	/**
	 * @see Expire
	 */
	boolean expire(String key, int seconds);

	/**
	 * @see PExpire
	 */
	boolean pexpire(String key, long milliseconds);

	/**
	 * @see ExpireAt
	 */
	boolean expireAt(String key, long unixTime);

	/**
	 * @see PExpireAt
	 */
	boolean pexpireAt(String key, long millisecondsTimestamp);

	/**
	 * @see Ttl
	 */
	long ttl(String key);

	/**
	 * @see PTtl
	 */
	long pttl(String key);

	/**
	 * @see SetBit
	 */
	boolean setbit(String key, long offset, boolean value);

	/**
	 * @see SetBit
	 */
	boolean setbit(String key, long offset, String value);

	/**
	 * @see GetBit
	 */
	boolean getbit(String key, long offset);

	/**
	 * @see SetRange
	 */
	long setrange(String key, long offset, String value);

	/**
	 * @see GetRange
	 */
	String getrange(String key, long startOffset, long endOffset);

	/**
	 * @see GetSet
	 */
	String getSet(String key, String value);

	/**
	 * @see SetNx
	 */
	boolean setnx(String key, String value);

	/**
	 * @see SetEx
	 */
	void setex(String key, int seconds, String value);

	/**
	 * @see PSetEx
	 */
	void psetex(String key, long milliseconds, String value);

	/**
	 * @see DecrementBy
	 */
	long decrBy(String key, long integer);

	/**
	 * @see Decrement
	 */
	long decr(String key);

	/**
	 * @see IncrementBy
	 */
	long incrBy(String key, long integer);

	/**
	 * @see IncrementByFloat
	 */
	double incrByFloat(String key, double value);

	/**
	 * @see Increment
	 */
	long incr(String key);

	/**
	 * @see Append
	 */
	long append(String key, String value);

	/**
	 * @see SubStr
	 */
	String substr(String key, int start, int end);

	/**
	 * @see HMSet
	 */
	boolean hset(String key, String field, String value);

	/**
	 * @see HGet
	 */
	String hget(String key, String field);

	/**
	 * @see HSetNx
	 */
	boolean hsetnx(String key, String field, String value);

	/**
	 * @see HMSet
	 */
	void hmset(String key, Map<String, String> hash);

	/**
	 * @see HMGet
	 */
	List<String> hmget(String key, String... fields);

	/**
	 * @see HIncrementBy
	 */
	long hincrBy(String key, String field, long value);

	/**
	 * @see HIncrementByFloat
	 */
	double hincrByFloat(String key, String field, double value);

	/**
	 * @see HExists
	 */
	boolean hexists(String key, String field);

	/**
	 * @see HDelete
	 */
	long hdel(String key, String... field);

	/**
	 * 返回键对应HASH上"字段-值"的对数
	 *
	 * @param key 键
	 * @return "字段-值"的对数
	 */
	Long hlen(String key);

	/**
	 * 返回键对应HASH上所有"字段-值"对的字段集合
	 *
	 * @param key 键
	 * @return 字段集合
	 */
	Set<String> hkeys(String key);

	/**
	 * 返回键对应HASH上所有"字段-值"对的值集合
	 *
	 * @param key 键
	 * @return 值集合
	 */
	List<String> hvals(String key);

	/**
	 * 返回键对应HASH上所有"字段-值"对
	 *
	 * @param key 键
	 * @return "字段-值"对Map
	 */
	Map<String, String> hgetAll(String key);


	Long rpush(String key, String... string);


	Long lpush(String key, String... string);


	Long llen(String key);


	List<String> lrange(String key, long start, long end);


	String ltrim(String key, long start, long end);


	String lindex(String key, long index);


	String lset(String key, long index, String value);


	Long lrem(String key, long count, String value);


	String lpop(String key);


	String rpop(String key);


	Long sadd(String key, String... member);


	Set<String> smembers(String key);


	Long srem(String key, String... member);


	String spop(String key);


	Set<String> spop(String key, long count);


	Long scard(String key);


	Boolean sismember(String key, String member);


	String srandmember(String key);


	List<String> srandmember(String key, int count);


	/**
	 * @see StrLen
	 */
	long strlen(String key);


	Long zadd(String key, double score, String member);


	//Long zadd(String key, double score, String member, ZAddParams params);


	Long zadd(String key, Map<String, Double> scoreMembers);


	//Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params);


	Set<String> zrange(String key, long start, long end);


	Long zrem(String key, String... member);


	Double zincrby(String key, double score, String member);


	//Double zincrby(String key, double score, String member, ZIncrByParams params);


	Long zrank(String key, String member);


	Long zrevrank(String key, String member);


	Set<String> zrevrange(String key, long start, long end);


	//Set<Tuple> zrangeWithScores(String key, long start, long end);


	//Set<Tuple> zrevrangeWithScores(String key, long start, long end);


	Long zcard(String key);


	Double zscore(String key, String member);


	List<String> sort(String key);


	//List<String> sort(String key, SortingParams sortingParameters);


	Long zcount(String key, double min, double max);


	Long zcount(String key, String min, String max);


	Set<String> zrangeByScore(String key, double min, double max);


	Set<String> zrangeByScore(String key, String min, String max);


	Set<String> zrevrangeByScore(String key, double max, double min);


	Set<String> zrangeByScore(String key, double min, double max, int offset, int count);


	Set<String> zrevrangeByScore(String key, String max, String min);


	Set<String> zrangeByScore(String key, String min, String max, int offset, int count);


	Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count);


	//Set<Tuple> zrangeByScoreWithScores(String key, double min, double max);


	//Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min);


	//Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count);


	Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count);


	//Set<Tuple> zrangeByScoreWithScores(String key, String min, String max);


	//Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min);


	//Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count);


	//Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count);


	//Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count);


	Long zremrangeByRank(String key, long start, long end);


	Long zremrangeByScore(String key, double start, double end);


	Long zremrangeByScore(String key, String start, String end);


	Long zlexcount(String key, String min, String max);


	Set<String> zrangeByLex(String key, String min, String max);


	Set<String> zrangeByLex(String key, String min, String max, int offset, int count);


	Set<String> zrevrangeByLex(String key, String max, String min);


	Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count);


	Long zremrangeByLex(String key, String min, String max);


	//Long linsert(String key, BinaryClient.LIST_POSITION where, String pivot, String value);


	Long lpushx(String key, String... string);


	Long rpushx(String key, String... string);


	List<String> blpop(String arg);


	List<String> blpop(int timeout, String key);


	List<String> brpop(String arg);


	List<String> brpop(int timeout, String key);


	/**
	 * @see Delete
	 */
	long del(String key);


	String echo(String string);


	Long move(String key, int dbIndex);


	Long bitcount(String key);


	Long bitcount(String key, long start, long end);


	Long bitpos(String key, boolean value);


	//Long bitpos(String key, boolean value, BitPosParams params);


	//ScanResult<Map.Entry<String, String>> hscan(String key, int cursor);


	//ScanResult<String> sscan(String key, int cursor);


	//ScanResult<Tuple> zscan(String key, int cursor);


	//ScanResult<Map.Entry<String, String>> hscan(String key, String cursor);


	//ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams params);


	//ScanResult<String> sscan(String key, String cursor);


	//ScanResult<String> sscan(String key, String cursor, ScanParams params);


	//ScanResult<Tuple> zscan(String key, String cursor);


	//ScanResult<Tuple> zscan(String key, String cursor, ScanParams params);


	Long pfadd(String key, String... elements);


	long pfcount(String key);


	Long geoadd(String key, double longitude, double latitude, String member);


	//Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap);


	Double geodist(String key, String member1, String member2);


	//Double geodist(String key, String member1, String member2, GeoUnit unit);


	List<String> geohash(String key, String... members);


	//List<GeoCoordinate> geopos(String key, String... members);


	//List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit);


	//List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param);


	//List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit);


	//List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param);


	List<Long> bitfield(String key, String... arguments);
}

