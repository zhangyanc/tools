package pers.zyc.tools.cacheclient.redis;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import pers.zyc.retry.BaseRetryPolicy;
import pers.zyc.retry.RetryFailedException;
import pers.zyc.retry.RetryLoop;
import redis.clients.jedis.JedisCommands;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * RedisCacheClient工厂bean, 实例为动态代理对象
 *
 * 屏蔽直接使用jedis时因redis部署不同导致的客户端差异、扩展重试功能
 *
 * @author zhangyancheng
 */
public abstract class AbstractJedisProxyFactoryBean<J extends JedisCommands>
		implements FactoryBean<RedisCacheClient>, InitializingBean, DisposableBean {

	/**
	 * 重试间隔
	 */
	private long retryDelay = 1000;
	/**
	 * 最大重试次数, 默认为0表示不启用重试
	 */
	private int retryTimes;

	/**
	 * 重试策略
	 */
	private BaseRetryPolicy retryPolicy;

	public void setRetryDelay(long retryDelay) {
		this.retryDelay = retryDelay;
	}

	/**
	 * 设置重试次数, 合法的正数表示启用重试
	 *
	 * @param retryTimes 重试次数
	 */
	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (retryTimes > 0) {
			retryPolicy = new BaseRetryPolicy() {

				@Override
				public Boolean handleException(Throwable cause, Callable<?> callable) {
					return handlePerRetryException(cause);
				}
			};
			retryPolicy.setMaxRetryTimes(retryTimes);
			retryPolicy.setRetryDelay(retryDelay);
		}
	}

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

	/**
	 * 处理重试异常, 只有开启了重试才有需要重写此方法
	 *
	 * @param cause 异常
	 * @return 是否继续重试
	 */
	protected boolean handlePerRetryException(Throwable cause) {
		return true;
	}

	/**
	 * 后置横切逻辑, 主要用于关闭jedis(资源回收)
	 */
	protected void afterPerInvoke(J jedis, Method method, Object[] args) {
	}

	/**
	 * Jedis InvocationHandler
	 *
	 * 将RedisCacheClient所有调用转给jedis执行
	 */
	protected class JedisInvocationHandler implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
			try {
				return RetryLoop.execute(new Callable<Object>() {

					@Override
					public Object call() throws Exception {
						J jedis = Objects.requireNonNull(getJedis());
						try {
							return method.invoke(jedis, args);
						} finally {
							afterPerInvoke(jedis, method, args);
						}
					}
				}, retryPolicy);
			} catch (RetryFailedException e) {
				//抛出重试操作的原始异常
				throw e.getCause();
			}
		}
	}
}
