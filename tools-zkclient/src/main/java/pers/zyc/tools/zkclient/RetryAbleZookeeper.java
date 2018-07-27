package pers.zyc.tools.zkclient;

import org.apache.zookeeper.KeeperException;
import pers.zyc.tools.utils.retry.*;
import pers.zyc.tools.zkclient.listener.ConnectionListener;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import static org.apache.zookeeper.KeeperException.*;

/**
 * 带重试的IZookeeper实现
 *
 * @author zhangyancheng
 */
class RetryAbleZookeeper extends DefaultZookeeper implements ConnectionListener, RetryCondition {

	/**
	 * 重试策略
	 */
	private final BaseRetryPolicy retryPolicy;

	RetryAbleZookeeper(ZKConnector connector, ClientConfig config) {
		super(connector);

		retryPolicy = new AwaitConnectedRetryPolicy(this);
		retryPolicy.setMaxRetryTimes(config.getRetryTimes());
		retryPolicy.setRetryDelay(config.getRetryPerWaitTimeout());

		connector.addListener(this);
	}

	/**
	 * 连接或者重连成功时更新ZooKeeper并唤醒重试等待线程
	 *
	 * @param newSession 是否为新会话
	 */
	@Override
	public void onConnected(boolean newSession) {
		synchronized (this) {
			//唤醒所有的重试等待线程
			this.notifyAll();
		}
	}

	@Override
	public void onDisconnected(boolean sessionClosed) {
	}

	@Override
	public Object getMutex() {
		return this;
	}

	@Override
	public boolean check() {
		//重试条件检查, 连接成功才重试
		return connector.isConnected();
	}

	/**
	 * 重试策略, 等待连接成功后重试
	 */
	private class AwaitConnectedRetryPolicy extends ConditionalRetryPolicy {

		AwaitConnectedRetryPolicy(RetryCondition retryCondition) {
			super(retryCondition);
		}

		@Override
		public Boolean handleException(Throwable throwable, Callable<?> callable) {
			logger.warn("Retry call exception: {}", throwable.getMessage());

			//当且仅当发生了KeeperException且为连接异常才进行重试
			return throwable instanceof KeeperException &&
							(throwable instanceof ConnectionLossException ||
							 throwable instanceof OperationTimeoutException ||
							 throwable instanceof SessionExpiredException ||
							 throwable instanceof SessionMovedException);
		}
	}

	@Override
	protected Object doInvoke(final Method method, final Object[] args) throws Exception {
		try {
			//重试执行
			return RetryLoop.execute(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					return RetryAbleZookeeper.super.doInvoke(method, args);
				}
			}, retryPolicy);
		} catch (RetryFailedException retryFailed) {
			logger.warn("Retry failed, {}", retryFailed.getRetryStat());

			//抛出重试操作的原始异常
			throw (Exception) retryFailed.getCause();
		}
	}
}
