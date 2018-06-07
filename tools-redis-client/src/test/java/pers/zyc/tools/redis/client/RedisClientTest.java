package pers.zyc.tools.redis.client;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * @author zhangyancheng
 */
public class RedisClientTest {

	public static void main(String[] args) throws Exception {
		ConnectionPool connectionPool = new ConnectionPool("redis://172.25.45.240:5274",
				new GenericObjectPoolConfig(),
				60000);
		connectionPool.start();

		RedisClient redisClient = new CustomRedisClient(connectionPool);
		System.out.println(redisClient.get("af"));

		System.out.println(redisClient.set("af", "av"));

		System.out.println(redisClient.get("af"));
	}
}
