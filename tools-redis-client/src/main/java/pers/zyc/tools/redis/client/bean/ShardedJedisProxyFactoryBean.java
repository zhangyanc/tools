package pers.zyc.tools.redis.client.bean;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.lang.reflect.Method;

/**
 * @author zhangyancheng
 */
public class ShardedJedisProxyFactoryBean extends RetryJedisProxyFactoryBean<ShardedJedis> {

	private final ShardedJedisPool shardedJedisPool;

	public ShardedJedisProxyFactoryBean(ShardedJedisPool shardedJedisPool) {
		this.shardedJedisPool = shardedJedisPool;
	}

	@Override
	public void destroy() throws Exception {
		shardedJedisPool.close();
	}

	@Override
	protected ShardedJedis getTarget() {
		return shardedJedisPool.getResource();
	}

	@Override
	protected void afterPerInvoke(ShardedJedis jedis, Method method, Object[] args) {
		jedis.close();
	}
}
