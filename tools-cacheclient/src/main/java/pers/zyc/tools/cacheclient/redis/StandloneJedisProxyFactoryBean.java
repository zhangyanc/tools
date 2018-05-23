package pers.zyc.tools.cacheclient.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author zhangyancheng
 */
public class StandloneJedisProxyFactoryBean extends AbstractJedisProxyFactoryBean<Jedis> {

	private final JedisPool jedisPool;

	public StandloneJedisProxyFactoryBean(JedisPool jedisPool) {
		this.jedisPool = Objects.requireNonNull(jedisPool);
	}

	@Override
	protected Jedis getJedis() {
		return jedisPool.getResource();
	}

	@Override
	protected void afterInvoke(Jedis jedis, Method method, Object[] args) {
		jedis.close();
	}

	@Override
	public void destroy() throws Exception {
		jedisPool.destroy();
	}
}
