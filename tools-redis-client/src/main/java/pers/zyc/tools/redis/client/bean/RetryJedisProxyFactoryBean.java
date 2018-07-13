package pers.zyc.tools.redis.client.bean;

import pers.zyc.tools.redis.client.exception.RedisClientException;
import pers.zyc.tools.utils.retry.BaseRetryPolicy;
import pers.zyc.tools.utils.retry.RetryFailedException;
import pers.zyc.tools.utils.retry.RetryLoop;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author zhangyancheng
 */
public abstract class RetryJedisProxyFactoryBean<J extends JedisCommands> extends JedisProxyFactoryBean<J> {
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

	/**
	 * 处理重试异常, 只有开启了重试才有需要重写此方法
	 *
	 * @param cause 异常
	 * @return 是否继续重试
	 */
	protected boolean handlePerRetryException(Throwable cause) {
		//只有连接异常才进行重试
		return cause instanceof JedisConnectionException;
	}

	@Override
	protected Object doInvoke(final Method method, final Object[] args) throws Exception {
		try {
			return RetryLoop.execute(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					return RetryJedisProxyFactoryBean.super.doInvoke(method, args);
				}
			}, retryPolicy);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RedisClientException("Retry Interrupted!");
		} catch (RetryFailedException e) {
			throw (Exception) e.getCause();
		}
	}
}
