package pers.zyc.tools.lifecycle;

import java.util.concurrent.TimeUnit;

/**
 * 带周期执行逻辑的服务类
 *
 * @author zhangyancheng
 */
public abstract class PeriodicService extends Service implements Thread.UncaughtExceptionHandler {
	private Thread periodicThread;

	@Override
	protected void doStart() {
		periodicThread = new PeriodicExecuteThread();
	}

	@Override
	protected void afterStart() {
		periodicThread.start();
	}

	@Override
	protected void doStop() throws Exception {
		periodicThread.interrupt();
	}

	/**
	 * 守护线程异常结束时可在这里记录或关闭服务
	 *
	 * @param t 线程
	 * @param e 异常
	 */
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		stop();
	}

	/**
	 * @return 休眠周期
	 */
	protected abstract long period();

	/**
	 * 周期执行逻辑
	 *
	 * @throws InterruptedException 线程被打断
	 */
	protected abstract void execute() throws InterruptedException;

	protected void onInterrupt() {
	}

	private class PeriodicExecuteThread extends Thread {

		{
			setName(getName());
			setDaemon(true);
			setUncaughtExceptionHandler(PeriodicService.this);
		}

		@Override
		public void run() {
			while (isRunning() && !isInterrupted()) {
				try {
					TimeUnit.MILLISECONDS.sleep(period());
					execute();
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		@Override
		public void interrupt() {
			onInterrupt();
			super.interrupt();
		}
	}
}
