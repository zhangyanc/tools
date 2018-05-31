package pers.zyc.tools.redis.client.bean;

import pers.zyc.tools.redis.client.ConnectionPool;
import pers.zyc.tools.redis.client.CustomRedisClient;
import pers.zyc.tools.redis.client.RedisClient;

/**
 * @author zhangyancheng
 */
public class CustomRedisClientFactoryBean extends RedisClientFactoryBean {

	private final ConnectionPool connectionPool;

	public CustomRedisClientFactoryBean(ConnectionPool connectionPool) {
		this.connectionPool = connectionPool;
	}

	@Override
	public RedisClient getObject() throws Exception {
		return new CustomRedisClient(connectionPool);
	}

	@Override
	public void destroy() throws Exception {
		connectionPool.stop();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		connectionPool.start();
	}
}
