package pers.zyc.tools.cacheclient.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author zhangyancheng
 */
public class SentineJedisProxyFactoryBean extends AbstractJedisProxyFactoryBean<Jedis> {

	private final JedisSentinelPool jedisSentinelPool;

	public SentineJedisProxyFactoryBean(JedisSentinelPool jedisSentinelPool) {
		this.jedisSentinelPool = Objects.requireNonNull(jedisSentinelPool);
	}

	@Override
	protected Jedis getJedis() {
		return jedisSentinelPool.getResource();
	}

	@Override
	protected void afterPerInvoke(Jedis jedis, Method method, Object[] args) {
		jedis.close();
	}

	@Override
	public void destroy() throws Exception {
		jedisSentinelPool.destroy();
	}
}
