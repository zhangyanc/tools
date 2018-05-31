package pers.zyc.tools.redis.client.bean;

import redis.clients.jedis.JedisCluster;

/**
 * @author zhangyancheng
 */
public class ClusterJedisProxyFactoryBean extends JedisProxyFactoryBean<JedisCluster> {

	private final JedisCluster jedisCluster;

	public ClusterJedisProxyFactoryBean(JedisCluster jedisCluster) {
		this.jedisCluster = jedisCluster;
	}

	@Override
	protected JedisCluster getTarget() {
		return jedisCluster;
	}

	@Override
	public void destroy() throws Exception {
		jedisCluster.close();
	}
}
