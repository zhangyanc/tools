package pers.zyc.tools.utils;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 命令线程工厂
 *
 * @see java.util.concurrent.Executors.DefaultThreadFactory
 */
public class NameThreadFactory implements ThreadFactory {

    private final String name;
    private final boolean daemon;
    private final AtomicInteger seq = new AtomicInteger(0);

    public NameThreadFactory(String name) {
        this(name, false);
    }

    public NameThreadFactory(String name, boolean daemon) {
        this.name = Objects.requireNonNull(name);
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Objects.requireNonNull(r);

        Thread newThread = new Thread(r, name + " - " + seq.getAndIncrement());
        newThread.setDaemon(daemon);
        return newThread;
    }
}
