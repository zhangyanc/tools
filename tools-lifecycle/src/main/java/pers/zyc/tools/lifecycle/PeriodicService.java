package pers.zyc.tools.lifecycle;

import java.util.Objects;

/**
 * 带周期执行逻辑的服务类
 *
 * @author zhangyancheng
 */
public abstract class PeriodicService extends Service implements Thread.UncaughtExceptionHandler {
    private Thread periodicThread;
    private Runnable periodic;

    @Override
    protected void beforeStart() throws Exception {
        if (periodic == null) {
            periodic = Objects.requireNonNull(createPeriodic());
        }
    }

    /**
     * @return 创建周期执行任务
     */
    protected abstract Periodic createPeriodic();

    @Override
    protected void afterStart() {
        periodicThread = new Thread(periodic, getName());
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
     * 守护线程结束时可在这里记录
     *
     * @param t 线程
     * @param e 异常
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
    }

    protected abstract class Periodic implements Runnable {

        /**
         * @return 检查周期任务是否存活
         */
        protected boolean isAlive() {
            return isRunning() && !periodicThread.isInterrupted();
        }

        /**
         * @return 周期间隔, 大于0休眠
         */
        protected long getPeriod() {
            return -1;
        }

        /**
         * 周期执行逻辑
         *
         * @throws InterruptedException 线程被打断
         */
        protected abstract void execute() throws InterruptedException;

        @Override
        public void run() {
            long period;
            while (isAlive()) {
                try {
                    period = getPeriod();
                    if (period > 0) {
                        Thread.sleep(period);
                    }
                    execute();
                } catch (InterruptedException e) {
                    periodicThread.interrupt();
                }
            }
        }

    }
}
