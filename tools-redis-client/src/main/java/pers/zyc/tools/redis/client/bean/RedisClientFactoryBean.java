package pers.zyc.tools.redis.client.bean;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author zhangyancheng
 */
public abstract class RedisClientFactoryBean<C>
		implements FactoryBean<C>, InitializingBean, DisposableBean {

	@Override
	public abstract Class<C> getObjectType();

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}

	@Override
	public void destroy() throws Exception {
	}
}
