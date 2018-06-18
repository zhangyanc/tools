package pers.zyc.tools.redis.client.bean;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import pers.zyc.tools.redis.client.CustomRedisClient;

/**
 * @author zhangyancheng
 */
public class CustomRedisClientFactoryBean extends RedisClientFactoryBean<CustomRedisClient> {
	private String connectStr;
	private int connectionTimeout;
	private int requestTimeout;
	private int netWorkers;
	private GenericObjectPoolConfig poolConfig;

	private CustomRedisClient redisClient;

	@Override
	public CustomRedisClient getObject() throws Exception {
		return redisClient;
	}

	@Override
	public Class<CustomRedisClient> getObjectType() {
		return CustomRedisClient.class;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		redisClient = new CustomRedisClient(connectStr, connectionTimeout, requestTimeout, netWorkers, poolConfig);
	}

	@Override
	public void destroy() throws Exception {
		redisClient.close();
	}

	public void setConnectStr(String connectStr) {
		this.connectStr = connectStr;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public void setRequestTimeout(int requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	public void setNetWorkers(int netWorkers) {
		this.netWorkers = netWorkers;
	}

	public void setPoolConfig(GenericObjectPoolConfig poolConfig) {
		this.poolConfig = poolConfig;
	}
}
