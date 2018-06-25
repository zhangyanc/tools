package pers.zyc.tools.redis.client.bean;

import pers.zyc.tools.redis.client.ClientConfig;
import pers.zyc.tools.redis.client.RedisClient;

/**
 * @author zhangyancheng
 */
public class CustomRedisClientFactoryBean extends RedisClientFactoryBean<RedisClient> {
	private ClientConfig clientConfig;

	private RedisClient redisClient;

	public CustomRedisClientFactoryBean(ClientConfig clientConfig) {
		this.clientConfig = clientConfig;
	}

	@Override
	public RedisClient getObject() throws Exception {
		return redisClient;
	}

	@Override
	public Class<RedisClient> getObjectType() {
		return RedisClient.class;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		redisClient = new RedisClient(clientConfig);
	}

	@Override
	public void destroy() throws Exception {
		redisClient.close();
	}
}
