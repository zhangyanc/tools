package pers.zyc.tools.cacheclient.redis;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import redis.clients.jedis.JedisCommands;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * RedisCacheClient工厂bean, 实例为动态代理对象
 *
 * 屏蔽直接使用jedis时因redis部署不同导致的客户端差异
 *
 * @author zhangyancheng
 */
public abstract class AbstractJedisProxyFactoryBean<J extends JedisCommands>
		implements FactoryBean<RedisCacheClient>, DisposableBean {

	@Override
	public RedisCacheClient getObject() throws Exception {
		//返回动态代理实例, 所有方法调用都由InvocationHandler代理执行
		return getObjectType().cast(
				Proxy.newProxyInstance(
					getClass().getClassLoader(),
					new Class<?>[] { RedisCacheClient.class },
					newJedisInvocationHandler()
				)
		);
	}

	@Override
	public Class<RedisCacheClient> getObjectType() {
		return RedisCacheClient.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	protected JedisInvocationHandler newJedisInvocationHandler() {
		return new JedisInvocationHandler();
	}

	/**
	 * 返回jedis(JedisCommands 接口的实现), 执行对redis的操作
	 *
	 * @return JedisCommands impl
	 */
	protected abstract J getJedis();

	protected void beforeInvoke(Method method, Object[] args) {
	}

	/**
	 * 后置横切逻辑, 用于关闭jedis(资源回收)
	 */
	protected void afterInvoke(J jedis, Method method, Object[] args) {
	}

	/**
	 * Jedis InvocationHandler
	 *
	 * 将RedisCacheClient所有调用转给jedis执行
	 */
	protected class JedisInvocationHandler implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			beforeInvoke(method, args);

			J jedis = Objects.requireNonNull(getJedis());
			try {
				return method.invoke(jedis, args);
			} finally {
				afterInvoke(jedis, method, args);
			}
		}
	}
}
