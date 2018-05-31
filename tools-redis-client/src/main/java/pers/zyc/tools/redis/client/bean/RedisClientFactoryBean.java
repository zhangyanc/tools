package pers.zyc.tools.redis.client.bean;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import pers.zyc.tools.redis.client.RedisClient;

/**
 * @author zhangyancheng
 */
public abstract class RedisClientFactoryBean
		implements FactoryBean<RedisClient>, InitializingBean, DisposableBean {

	@Override
	public Class<RedisClient> getObjectType() {
		return RedisClient.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void destroy() throws Exception {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}
}
