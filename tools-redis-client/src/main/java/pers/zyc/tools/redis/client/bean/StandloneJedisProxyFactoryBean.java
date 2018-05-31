package pers.zyc.tools.redis.client.bean;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Method;

/**
 * @author zhangyancheng
 */
public class StandloneJedisProxyFactoryBean extends RetryJedisProxyFactoryBean<Jedis> {

	private final JedisPool jedisPool;

	public StandloneJedisProxyFactoryBean(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	@Override
	public void destroy() throws Exception {
		jedisPool.close();
	}

	@Override
	protected Jedis getTarget() {
		return jedisPool.getResource();
	}

	@Override
	protected void afterPerInvoke(Jedis jedis, Method method, Object[] args) {
		jedis.close();
	}
}
