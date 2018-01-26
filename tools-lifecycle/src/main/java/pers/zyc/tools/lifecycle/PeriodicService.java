package pers.zyc.tools.lifecycle;

import java.util.concurrent.TimeUnit;

/**
 * 带周期执行逻辑的服务类
 *
 * @author zhangyancheng
 */
public abstract class PeriodicService extends Service implements Thread.UncaughtExceptionHandler, Runnable {
    private Thread periodicThread;

    @Override
    protected void doStart() {
    }

    @Override
    protected void afterStart() {
        periodicThread = new Thread(this, getName());
        periodicThread.setUncaughtExceptionHandler(this);
        periodicThread.setDaemon(true);
        periodicThread.start();
    }

    @Override
    protected void doStop() throws Exception {
        periodicThread.interrupt();
        periodicThread = null;
    }

    /**
     * @return 休眠周期
     */
    protected abstract long period();

    /**
     * @return 检查存活
     */
    protected boolean isAlive() {
        return isRunning() && !periodicThread.isInterrupted();
    }

    /**
     * 周期执行逻辑
     *
     * @throws InterruptedException 线程被打断
     */
    protected abstract void execute() throws InterruptedException;

    @Override
    public final void run() {
        while (isAlive()) {
            try {
                TimeUnit.MILLISECONDS.sleep(period());
                execute();
            } catch (InterruptedException e) {
                periodicThread.interrupt();
            }
        }
    }

    /**
     * 守护线程结束时可在这里记录异常
     *
     * @param t 线程
     * @param e 异常
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
    }
}
