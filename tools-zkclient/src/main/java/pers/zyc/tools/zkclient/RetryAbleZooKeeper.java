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
class RetryAbleZooKeeper extends DefaultZooKeeper {

	private final BaseRetryPolicy retryPolicy;

	RetryAbleZooKeeper(ZKClient zkClient) {
		super(zkClient);

		ClientConfig config = zkClient.getConfig();
		RetryConditionListener retryCondition = new RetryConditionListener();
		retryPolicy = new AwaitConnectedRetryPolicy(retryCondition);
		retryPolicy.setMaxRetryTimes(config.getRetryTimes());
		retryPolicy.setRetryDelay(config.getRetryPerWaitTimeout());

		zkClient.addConnectionListener(retryCondition);
	}

	private class RetryConditionListener implements ConnectionListener, RetryCondition {

		@Override
		public boolean check() {
			//重试条件检查, 连接成功才重试
			return zkClient.isConnected();
		}

		@Override
		public Object getMutex() {
			return this;
		}

		@Override
		public void onConnected(boolean newSession) {
			synchronized (this) {
				//唤醒所有的重试等待线程
				notifyAll();
			}
		}

		@Override
		public void onDisconnected(boolean sessionClosed) {
		}
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

	private class IdempotentCall implements Callable<Object> {
		final Method method;
		final Object[] args;
		boolean isRetry = false;

		private IdempotentCall(Method method, Object[] args) {
			this.method = method;
			this.args = args;
		}

		@Override
		public Object call() throws Exception {
			try {
				return invoke(method, args);
			} finally {
				isRetry = true;
			}
		}
	}

	private class CreateCall extends IdempotentCall {

		private CreateCall(Method method, Object[] args) {
			super(method, args);
		}

		@Override
		public Object call() throws Exception {
			return super.call();
		}
	}

	@Override
	public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
		IdempotentCall zooKeeperCall;
		switch (method.getName()) {
			case "create":
			case "delete":
			case "setData":
			case "multi":
				zooKeeperCall = new CreateCall(method, args);
				break;
			default:
				zooKeeperCall = new IdempotentCall(method, args);
		}

		try {
			//重试执行
			return RetryLoop.execute(zooKeeperCall, retryPolicy);
		} catch (RetryFailedException retryFailed) {
			logger.warn("Retry failed, {}", retryFailed.getRetryStat());
			//抛出重试操作的原始异常
			throw retryFailed.getCause();
		}
	}
}
