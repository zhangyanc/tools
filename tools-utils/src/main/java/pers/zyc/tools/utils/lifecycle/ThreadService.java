package pers.zyc.tools.utils.lifecycle;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangyancheng
 */
public abstract class ThreadService extends Service {

	private Thread serviceThread;

	private ThreadFactory threadFactory;

	@Override
	protected void doStart() {
	}

	@Override
	protected void afterStart() {
		Runnable runnable = getRunnable();
		if (threadFactory != null) {
			serviceThread = threadFactory.newThread(runnable);
		} else {
			serviceThread = new Thread(runnable, getName());
		}
		serviceThread.start();
	}

	@Override
	protected void doStop() throws Exception {
		if (serviceThread.isAlive()) {
			serviceThread.interrupt();
		}
	}

	public void setThreadFactory(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
	}

	/**
	 * 检查当前线程是否服务线程
	 */
	public boolean currentInThreading() {
		return isServiceThread(Thread.currentThread());
	}

	/**
	 * 检查给定线程是否服务线程
	 */
	public boolean isServiceThread(Thread thread) {
		return thread == serviceThread;
	}

	protected abstract Runnable getRunnable();

	protected abstract class ServiceRunnable implements Runnable {

		protected boolean isAlive() {
			return isRunning() && !serviceThread.isInterrupted();
		}

		/**
		 * 间隔
		 */
		protected abstract long getInterval();

		/**
		 * 服务逻辑
		 */
		protected abstract void execute() throws InterruptedException;

		@Override
		public void run() {
			while (isAlive()) {
				try {
					TimeUnit.MILLISECONDS.sleep(getInterval());
					execute();
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}
}
