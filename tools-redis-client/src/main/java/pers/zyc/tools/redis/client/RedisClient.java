package pers.zyc.tools.redis.client;


import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 方法声明与JedisCommands完全相同(不从JedisCommands继承是因为提供了完整的实现, 可不依赖jedis lib)
 *
 * @author zhangyancheng
 */
public interface RedisClient {

	/**
	 * 将字符串值设置到键, 如果键已存在则覆盖就值, 且无视类型
	 *
	 * @param key key
	 * @param value value
	 * @return 响应码
	 */
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
	String set(String key, String value, String nxxx);

	/**
	 * 获取键设置的的字符串值
	 *
	 * @param key 键
	 * @return 如果键不存在返回null
	 */
	String get(String key);

	/**
	 * 检查键是否存在
	 *
	 * @param key 键
	 * @return 键是否存在
	 */
	Boolean exists(String key);

	/**
	 * 取消{@link #expire(String, int) expire}设置在键上的超时时间
	 *
	 * @param key 键
	 * @return 键不存在返回0, 否者返回1
	 */
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
	String type(String key);

	/**
	 * 在键上设置一个过期时间, 过期后键将在服务端被自动删除
	 *
	 * @param key 键
	 * @param seconds 过期秒数
	 * @return 1 -- 成功设置了过期时间, 0 -- 键不存在.(键已有过期时间时, Redis 2.1.3之后的版本
	 * 		   会更新时间并返回1, 之前的版本不更新时间返回0)
	 */
	Long expire(String key, int seconds);

	/**
	 * 和{@link #expire(String, int) expire}一样, 不过过期时间为一个毫秒数
	 *
	 * @param key 键
	 * @param milliseconds 过期毫秒数
	 * @return 响应码, 同expire.
	 */
	Long pexpire(String key, long milliseconds);

	/**
	 * 和{@link #expire(String, int) expire}一样, 不过过期时间是一个用秒数表示的绝对unix时间点
	 *
	 * @param key 键
	 * @param unixTime 过期时间点
	 * @return 响应码, 同expire
	 */
	Long expireAt(String key, long unixTime);

	/**
	 * 和{@link #expireAt(String, long) expire}一样, 不过过期时间是一个用毫秒数表示的绝对unix时间点
	 *
	 * @param key 键
	 * @param millisecondsTimestamp 过期时间点
	 * @return 响应码, 同expire
	 */
	Long pexpireAt(String key, long millisecondsTimestamp);

	/**
	 * 检查键的剩余过期时间秒数
	 *
	 * @param key 键
	 * @return 键剩余过期秒数, 在Redis 2.6及之前版本, 如果键不存在或者未设置过期时间都将返回-1,
	 * 		   但在2.8及之后的版本, 如果键不存在返回-2, 键未设置过期时间返回-1
	 *
	 */
	Long ttl(String key);

	/**
	 * 和{@link #ttl(String) ttl}一样, 不过返回的是毫秒数
	 *
	 * @param key 键
	 * @return 同ttl
	 */
	Long pttl(String key);

	Boolean setbit(String key, long offset, boolean value);

	Boolean setbit(String key, long offset, String value);

	Boolean getbit(String key, long offset);

	Long setrange(String key, long offset, String value);

	String getrange(String key, long startOffset, long endOffset);

	/**
	 * 将字符串值设置到键, 并返回键原先的值
	 *
	 * @param key 键
	 * @param value 字符串值
	 * @return 键原先的值, 如果键不存在返回null
	 */
	String getSet(String key, String value);

	/**
	 * 将字符串值设置到键, 当且仅当键不存在才设置成功
	 *
	 * @param key 键
	 * @param value 字符串值
	 * @return 1 -- 设置成功, 0 -- 设置未成功
	 */
	Long setnx(String key, String value);

	/**
	 * 将字符串值设置到键, 同时设置过期时间
	 *
	 * @param key 键
	 * @param seconds 过期时间秒数
	 * @param value 字符串值
	 * @return 响应码
	 */
	String setex(String key, int seconds, String value);

	/**
	 * 将字符串值设置到键, 同时设置过期时间
	 *
	 * @param key 键
	 * @param milliseconds 过期时间毫秒数
	 * @param value 字符串值
	 * @return 响应吗
	 */
	String psetex(String key, long milliseconds, String value);

	/**
	 * 将键设置的数值减去减数并返回结果值
	 *
	 * @param key 键
	 * @param integer 减数
	 * @return 减后的值
	 */
	Long decrBy(String key, long integer);

	/**
	 * 将键设置的数值减1并返回结果值
	 *
	 * @param key 键
	 * @return 减后的值
	 */
	Long decr(String key);

	/**
	 * 将键设置的数值加上加数并返回结果值
	 *
	 * @param key 键
	 * @param integer 加数
	 * @return 加后的值
	 */
	Long incrBy(String key, long integer);

	/**
	 * 将键设置的数值加上加数并返回结果值
	 *
	 * @param key 键
	 * @param value 加数(浮点数)
	 * @return 加后的值
	 */
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
	Long incr(String key);

	/**
	 * 将给定的字符串追加到键已设置的值, 如果键不存在则等同于set操作
	 *
	 * @param key 键
	 * @param value 字符串值
	 * @return 追加后的字符串长度
	 */
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
	String substr(String key, int start, int end);

	/**
	 * 在键对应的HASH上设置一对"字段-值", 如果键不存在则新增后再设置, 字段已存在则更新值
	 *
	 * @param key 键
	 * @param field 字段
	 * @param value 值
	 * @return 字段已存在时更新值并返回0, 否则返回1
	 */
	Long hset(String key, String field, String value);

	/**
	 * 返回键对应HASH上字段设置的值, 如果键不存在或者字段不存在返回null
	 *
	 * @param key 键
	 * @param field 字段
	 * @return 字段值或者null
	 */
	String hget(String key, String field);

	/**
	 * 在键对应的HASH上设置一对"字段-值", 当且仅当字段不存在时才设置
	 *
	 * @param key 键
	 * @param field 字段
	 * @param value 值
	 * @return 如果字段已存在返回0, 否则返回1
	 */
	Long hsetnx(String key, String field, String value);

	/**
	 * 在键对应的HASH上设置多对"字段-值", 如果键不存在则新增后再设置, 字段已存在则更新值
	 *
	 * @param key 键
	 * @param hash "字段-值"Map集合
	 * @return 响应码
	 */
	String hmset(String key, Map<String, String> hash);

	/**
	 * 返回键对应HASH上多个字段设置的值, 如果键不存在或者字段不存在返回null
	 *
	 * @param key 键
	 * @param fields 字段
	 * @return 多个字段值
	 */
	List<String> hmget(String key, String... fields);

	/**
	 * 将键对应HASH上字段的数值加上加数, 如果键不存在则新建, 字段不存在则按照0计算
	 *
	 * @param key 键
	 * @param field 字段
	 * @param value 加数
	 * @return 加后的值
	 */
	Long hincrBy(String key, String field, long value);

	/**
	 * 将键对应HASH上字段的数值加上加数, 如果键不存在则新建, 字段不存在则按照0计算
	 *
	 * @param key 键
	 * @param field 字段
	 * @param value 加数(浮点数)
	 * @return 加后的值
	 */
	Double hincrByFloat(String key, String field, double value);

	/**
	 * 检查键对应HASH上字段是否存在
	 *
	 * @param key 键
	 * @param field 值
	 * @return 字段是否存在
	 */
	Boolean hexists(String key, String field);

	/**
	 * 删除键对应HASH上"字段-值"对
	 *
	 * @param key 键
	 * @param field 字段
	 * @return 字段存在被删除后返回1, 否则返回0
	 */
	Long hdel(String key, String... field);

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

	
	Long strlen(String key);

	
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

	
	Long del(String key);

	
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
