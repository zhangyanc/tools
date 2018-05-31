package pers.zyc.tools.redis.client.bean;

import pers.zyc.tools.redis.client.RedisClient;
import pers.zyc.tools.redis.client.RedisClientException;
import redis.clients.jedis.JedisCommands;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * @author zhangyancheng
 */
public abstract class JedisProxyFactoryBean<J extends JedisCommands> extends RedisClientFactoryBean {

	@Override
	public RedisClient getObject() throws Exception {
		/*
		 * 返回动态代理实例, 所有方法调用都由InvocationHandler代理执行
		 *
		 * JedisCommands与RedisClient有相同的方法声明, 却不具有继承关系
		 * 因此设置代理接口时, JedisCommands.class必须在RedisClient.class前面,
		 * 使得代理对象内部的Method实例的所属Class为JedisCommands, 否则无法在InvocationHandler中反射调用
		 */
		return getObjectType().cast(
				Proxy.newProxyInstance(
						getClass().getClassLoader(),
						new Class<?>[] { JedisCommands.class, RedisClient.class },
						newJedisInvocationHandler()
				)
		);
	}

	protected JedisInvocationHandler newJedisInvocationHandler() {
		return new JedisInvocationHandler();
	}

	/**
	 * 返回jedis(JedisCommands 接口的实现), 执行对redis的操作
	 *
	 * @return JedisCommands impl
	 */
	protected abstract J getTarget();

	/**
	 * 将调用转给jedis target反射执行, 并返回结果
	 *
	 * @param method 调用方法
	 * @param args 调用参数
	 * @return 调用返回值
	 * @throws Exception 异常
	 */
	protected Object doInvoke(Method method, Object[] args) throws Exception {
		return doInvoke(getTarget(), method, args);
	}

	private Object doInvoke(J jedis, Method method, Object[] args) throws Exception {
		Objects.requireNonNull(jedis);
		try {
			return reflectInvoke(jedis, method, args);
		} finally {
			afterPerInvoke(jedis, method, args);
		}
	}

	/**
	 * 后置横切逻辑, 主要用于关闭jedis(资源回收)
	 */
	protected void afterPerInvoke(J jedis, Method method, Object[] args) {
	}

	protected class JedisInvocationHandler implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return doInvoke(method, args);
		}
	}

	private static Object reflectInvoke(JedisCommands jedis, Method method, Object[] args) throws Exception {
		try {
			return method.invoke(jedis, args);
		} catch (RuntimeException rte) {
			throw rte;
		} catch (InvocationTargetException e) {
			//抛出反射调用的原始异常
			Throwable targetException = e.getTargetException();
			if (targetException instanceof Exception) {
				throw (Exception) targetException;
			} else {
				throw new RedisClientException(e);
			}
		} catch (Throwable e) {
			throw new RedisClientException(e);
		}
	}
}
