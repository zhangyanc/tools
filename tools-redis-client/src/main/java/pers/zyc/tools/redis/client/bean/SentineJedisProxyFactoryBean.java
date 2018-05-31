package pers.zyc.tools.redis.client.bean;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.lang.reflect.Method;

/**
 * @author zhangyancheng
 */
public class SentineJedisProxyFactoryBean extends RetryJedisProxyFactoryBean<Jedis> {

	private final JedisSentinelPool jedisSentinelPool;

	public SentineJedisProxyFactoryBean(JedisSentinelPool jedisSentinelPool) {
		this.jedisSentinelPool = jedisSentinelPool;
	}

	@Override
	public void destroy() throws Exception {
		jedisSentinelPool.close();
	}

	@Override
	protected Jedis getTarget() {
		return jedisSentinelPool.getResource();
	}

	@Override
	protected void afterPerInvoke(Jedis jedis, Method method, Object[] args) {
		jedis.close();
	}
}
