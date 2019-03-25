package pers.zyc.tools.network;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @author zhangyancheng
 */
public abstract class BaseRequestHandler implements RequestHandler {
	/**
	 * 在提交任务线程中执行
	 */
	private static final Executor SYNC_EXECUTOR = new Executor() {

		@Override
		public void execute(Runnable runnable) {
			runnable.run();
		}
	};

	/**
	 * 请求处理执行器
	 */
	private Executor executor = SYNC_EXECUTOR;

	@Override
	public Executor getExecutor() {
		return executor;
	}

	/**
	 * 设置请求处理执行器
	 *
	 * @param executor 执行器
	 */
	public void setExecutor(Executor executor) {
		this.executor = Objects.requireNonNull(executor);
	}
}
