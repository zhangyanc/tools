package pers.zyc.tools.redis.client;

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
public interface RedisClient extends JedisCommands {

	/**
	 * 将字符串值设置到键, 如果键已存在则覆盖就值, 且无视类型
	 *
	 * @param key key
	 * @param value value
	 * @return 响应码
	 */
	@Override
	String set(String key, String value);

	/**
	 * 将字符串值设置到键, 并且设置过期时间
	 *
	 * @param key 键
	 * @param value 字符串值
	 * @param nxxx 可选值为"NX"或者XX
	 *             NX -- 当且仅当键不存在才设置
	 *             XX -- 当前仅当键存在才设置
	 * @param expx 过期时间单位,可选值为"EX"或者"PX"
	 *             EX -- 秒
	 *             PX --毫秒
	 * @param time 过期时间
	 * @return 响应码
	 */
	@Override
	String set(String key, String value, String nxxx, String expx, long time);

	/**
	 * 将字符串值设置到键
	 *
	 * @param key 键
	 * @param value 字符串值
	 * @param nxxx 可选值为"NX"或者XX
	 *             NX -- 当且仅当键不存在才设置
	 *             XX -- 当前仅当键存在才设置
	 * @return 响应码
	 */
	@Override
	String set(String key, String value, String nxxx);

	/**
	 * 获取键设置的的字符串值
	 *
	 * @param key 键
	 * @return 如果键不存在返回null
	 */
	@Override
	String get(String key);

	/**
	 * 检查键是否存在
	 *
	 * @param key 键
	 * @return 键是否存在
	 */
	@Override
	Boolean exists(String key);

	/**
	 * 取消{@link #expire(String, int) expire}设置在键上的超时时间
	 *
	 * @param key 键
	 * @return 键不存在返回0, 否者返回1
	 */
	@Override
	Long persist(String key);

	/**
	 * 返回键存储的值类型, 如果键不存在返回特殊字符串"none"
	 *
	 * @param key 键
	 * @return "none" -- 键不存在
	 * 		   "string" -- 字符串类型
	 * 		   "list" -- list类型
	 * 		   "set" -- set类型
	 * 		   "zset" -- 排序set
	 * 		   "hash" -- 哈希表
	 */
	@Override
	String type(String key);

	/**
	 * 在键上设置一个过期时间, 过期后键将在服务端被自动删除
	 *
	 * @param key 键
	 * @param seconds 过期秒数
	 * @return 1 -- 成功设置了过期时间, 0 -- 键不存在.(键已有过期时间时, Redis 2.1.3之后的版本
	 * 		   会更新时间并返回1, 之前的版本不更新时间返回0)
	 */
	@Override
	Long expire(String key, int seconds);

	/**
	 * 和{@link #expire(String, int) expire}一样, 不过过期时间为一个毫秒数
	 *
	 * @param key 键
	 * @param milliseconds 过期毫秒数
	 * @return 响应码, 同expire.
	 */
	@Override
	Long pexpire(String key, long milliseconds);

	/**
	 * 和{@link #expire(String, int) expire}一样, 不过过期时间是一个用秒数表示的绝对unix时间点
	 *
	 * @param key 键
	 * @param unixTime 过期时间点
	 * @return 响应码, 同expire
	 */
	@Override
	Long expireAt(String key, long unixTime);

	/**
	 * 和{@link #expireAt(String, long) expire}一样, 不过过期时间是一个用毫秒数表示的绝对unix时间点
	 *
	 * @param key 键
	 * @param millisecondsTimestamp 过期时间点
	 * @return 响应码, 同expire
	 */
	@Override
	Long pexpireAt(String key, long millisecondsTimestamp);

	/**
	 * 检查键的剩余过期时间秒数
	 *
	 * @param key 键
	 * @return 键剩余过期秒数, 在Redis 2.6及之前版本, 如果键不存在或者未设置过期时间都将返回-1,
	 * 		   但在2.8及之后的版本, 如果键不存在返回-2, 键未设置过期时间返回-1
	 *
	 */
	@Override
	Long ttl(String key);

	/**
	 * 和{@link #ttl(String) ttl}一样, 不过返回的是毫秒数
	 *
	 * @param key 键
	 * @return 同ttl
	 */
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

	/**
	 * 将字符串值设置到键, 并返回键原先的值
	 *
	 * @param key 键
	 * @param value 字符串值
	 * @return 键原先的值, 如果键不存在返回null
	 */
	@Override
	String getSet(String key, String value);

	/**
	 * 将字符串值设置到键, 当且仅当键不存在才设置成功
	 *
	 * @param key 键
	 * @param value 字符串值
	 * @return 1 -- 设置成功, 0 -- 设置未成功
	 */
	@Override
	Long setnx(String key, String value);

	/**
	 * 将字符串值设置到键, 同时设置过期时间
	 *
	 * @param key 键
	 * @param seconds 过期时间秒数
	 * @param value 字符串值
	 * @return 响应码
	 */
	@Override
	String setex(String key, int seconds, String value);

	/**
	 * 将字符串值设置到键, 同时设置过期时间
	 *
	 * @param key 键
	 * @param milliseconds 过期时间毫秒数
	 * @param value 字符串值
	 * @return 响应吗
	 */
	@Override
	String psetex(String key, long milliseconds, String value);

	/**
	 * 将键设置的数值减去减数并返回结果值
	 *
	 * @param key 键
	 * @param integer 减数
	 * @return 减后的值
	 */
	@Override
	Long decrBy(String key, long integer);

	/**
	 * 将键设置的数值减1并返回结果值
	 *
	 * @param key 键
	 * @return 减后的值
	 */
	@Override
	Long decr(String key);

	/**
	 * 将键设置的数值加上加数并返回结果值
	 *
	 * @param key 键
	 * @param integer 加数
	 * @return 加后的值
	 */
	@Override
	Long incrBy(String key, long integer);

	/**
	 * 将键设置的数值加上加数并返回结果值
	 *
	 * @param key 键
	 * @param value 加数(浮点数)
	 * @return 加后的值
	 */
	@Override
	Double incrByFloat(String key, double value);

	/**
	 * 将键设置的数值加1并返回结果值
	 *
	 * <p>
	 * 		如果key不存在将以0为起始数进行操作,
	 * 		Redis没有专有的整数类型, 所有的加、减操作都发生在string类型的10进制64位数字值上,
	 * 		返回值也是将string转换为数字返回
	 *
	 *
	 * @param key 键
	 * @return 加后的值
	 */
	@Override
	Long incr(String key);

	/**
	 * 将给定的字符串追加到键已设置的值, 如果键不存在则等同于set操作
	 *
	 * @param key 键
	 * @param value 字符串值
	 * @return 追加后的字符串长度
	 */
	@Override
	Long append(String key, String value);

	/**
	 * 返回键上设置的字符串值的子串, 其中起始、结束位置都会被包含
	 *
	 * <p>
	 *     负数下标表示从字符串的末尾开始计算(-1表示最后一个, -2表示倒数第二个...依次类推)
	 *
	 *     substr不会反馈越界异常, 而是按照字符串实际长度返回
	 *
	 * @param key 键
	 * @param start 开始位置
	 * @param end 结束位置
	 * @return 子串
	 */
	@Override
	String substr(String key, int start, int end);

	/**
	 * 在键对应的HASH上设置一对"字段-值", 如果键不存在则新增后再设置, 字段已存在则更新值
	 *
	 * @param key 键
	 * @param field 字段
	 * @param value 值
	 * @return 字段已存在时更新值并返回0, 否则返回1
	 */
	@Override
	Long hset(String key, String field, String value);

	/**
	 * 返回键对应HASH上字段设置的值, 如果键不存在或者字段不存在返回null
	 *
	 * @param key 键
	 * @param field 字段
	 * @return 字段值或者null
	 */
	@Override
	String hget(String key, String field);

	/**
	 * 在键对应的HASH上设置一对"字段-值", 当且仅当字段不存在时才设置
	 *
	 * @param key 键
	 * @param field 字段
	 * @param value 值
	 * @return 如果字段已存在返回0, 否则返回1
	 */
	@Override
	Long hsetnx(String key, String field, String value);

	/**
	 * 在键对应的HASH上设置多对"字段-值", 如果键不存在则新增后再设置, 字段已存在则更新值
	 *
	 * @param key 键
	 * @param hash "字段-值"Map集合
	 * @return 响应码
	 */
	@Override
	String hmset(String key, Map<String, String> hash);

	/**
	 * 返回键对应HASH上多个字段设置的值, 如果键不存在或者字段不存在返回null
	 *
	 * @param key 键
	 * @param fields 字段
	 * @return 多个字段值
	 */
	@Override
	List<String> hmget(String key, String... fields);

	/**
	 * 将键对应HASH上字段的数值加上加数, 如果键不存在则新建, 字段不存在则按照0计算
	 *
	 * @param key 键
	 * @param field 字段
	 * @param value 加数
	 * @return 加后的值
	 */
	@Override
	Long hincrBy(String key, String field, long value);

	/**
	 * 将键对应HASH上字段的数值加上加数, 如果键不存在则新建, 字段不存在则按照0计算
	 *
	 * @param key 键
	 * @param field 字段
	 * @param value 加数(浮点数)
	 * @return 加后的值
	 */
	@Override
	Double hincrByFloat(String key, String field, double value);

	/**
	 * 检查键对应HASH上字段是否存在
	 *
	 * @param key 键
	 * @param field 值
	 * @return 字段是否存在
	 */
	@Override
	Boolean hexists(String key, String field);

	/**
	 * 删除键对应HASH上"字段-值"对
	 *
	 * @param key 键
	 * @param field 字段
	 * @return 字段存在被删除后返回1, 否则返回0
	 */
	@Override
	Long hdel(String key, String... field);

	/**
	 * 返回键对应HASH上"字段-值"的对数
	 *
	 * @param key 键
	 * @return "字段-值"的对数
	 */
	@Override
	Long hlen(String key);

	/**
	 * 返回键对应HASH上所有"字段-值"对的字段集合
	 *
	 * @param key 键
	 * @return 字段集合
	 */
	@Override
	Set<String> hkeys(String key);

	/**
	 * 返回键对应HASH上所有"字段-值"对的值集合
	 *
	 * @param key 键
	 * @return 值集合
	 */
	@Override
	List<String> hvals(String key);

	/**
	 * 返回键对应HASH上所有"字段-值"对
	 *
	 * @param key 键
	 * @return "字段-值"对Map
	 */
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
