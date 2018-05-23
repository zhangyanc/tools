package pers.zyc.tools.cacheclient.redis;

import redis.clients.jedis.JedisCluster;

import java.util.Objects;

/**
 * @author zhangyancheng
 */
public class ClusterJedisProxyFactoryBean extends AbstractJedisProxyFactoryBean<JedisCluster> {

	private final JedisCluster jedisCluster;

	public ClusterJedisProxyFactoryBean(JedisCluster jedisCluster) {
		this.jedisCluster = Objects.requireNonNull(jedisCluster);
	}

	@Override
	protected JedisCluster getJedis() {
		return jedisCluster;
	}

	@Override
	public void destroy() throws Exception {
		jedisCluster.close();
	}
}
