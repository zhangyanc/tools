package pers.zyc.tools.cacheclient.redis;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author zhangyancheng
 */
public class ShardedJedisProxyFactoryBean extends AbstractJedisProxyFactoryBean<ShardedJedis> {

	private final ShardedJedisPool shardedJedisPool;

	public ShardedJedisProxyFactoryBean(ShardedJedisPool shardedJedisPool) {
		this.shardedJedisPool = Objects.requireNonNull(shardedJedisPool);
	}

	@Override
	protected ShardedJedis getJedis() {
		return shardedJedisPool.getResource();
	}

	@Override
	protected void afterPerInvoke(ShardedJedis jedis, Method method, Object[] args) {
		jedis.close();
	}

	@Override
	public void destroy() throws Exception {
		shardedJedisPool.destroy();
	}
}
