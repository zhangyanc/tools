package pers.zyc.tools.utils;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 命令线程工厂
 *
 * @see java.util.concurrent.Executors.DefaultThreadFactory
 */
public class GeneralThreadFactory implements ThreadFactory {

    private final String name;
	private AtomicInteger threadNumber;

    private boolean daemon;
    private ThreadGroup group;
    private int priority = Thread.NORM_PRIORITY;
    private Thread.UncaughtExceptionHandler exceptionHandler;

    public GeneralThreadFactory(String name) {
        this.name = Objects.requireNonNull(name);
        if (name.endsWith("-")) {
            threadNumber = new AtomicInteger();
        }
    }

    @Override
	public Thread newThread(Runnable r) {
		String threadName = this.name;
		if (threadNumber != null) {
            threadName += threadNumber.incrementAndGet();
        }
		Thread newThread = new Thread(group, r, threadName);
		newThread.setDaemon(daemon);
        newThread.setPriority(priority);
        newThread.setUncaughtExceptionHandler(exceptionHandler);
		return newThread;
	}

    public String getName() {
        return name;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public ThreadGroup getGroup() {
        return group;
    }

    public void setGroup(ThreadGroup group) {
        this.group = group;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        if (priority > Thread.MAX_PRIORITY || priority < Thread.MIN_PRIORITY) {
            return;
        }
        this.priority = priority;
    }

    public Thread.UncaughtExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(Thread.UncaughtExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
}
