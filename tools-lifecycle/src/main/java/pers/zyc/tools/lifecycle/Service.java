package pers.zyc.tools.lifecycle;

import pers.zyc.tools.utils.LockHolder;
import pers.zyc.tools.utils.Stateful;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhangyancheng
 */
public abstract class Service implements Lifecycle, LockHolder<Lock>, Stateful<ServiceState> {

    private volatile ServiceState serviceState = ServiceState.NEW;
    private Lock serviceLock = Objects.requireNonNull(initServiceLock());

    @Override
    public ServiceState getState() {
        return serviceState;
    }

    @Override
    public void setState(ServiceState serviceState) {
        this.serviceState = serviceState;
    }

    @Override
    public boolean checkState(ServiceState serviceState) {
        return this.serviceState == serviceState;
    }

    @Override
    public Lock getLock() {
        return serviceLock;
    }

    /**
     * 初始化锁
     */
    protected Lock initServiceLock() {
        return new ReentrantLock(true);
    }

    /**
     * @return 服务名
     */
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * 检查服务是否处于RUNNING状态, 不是则抛出异常
     * @throws ServiceException.NotRunningException service not running
     */
    public void checkRunning() {
        if (!isRunning()) {
            throw new ServiceException.NotRunningException();
        }
    }

    @Override
    public boolean isRunning() {
        return checkState(ServiceState.RUNNING);
    }

    @Override
    public void start() {
        final Lock lock = getLock();
        lock.lock();
        try {
            if (isRunning()) {
                return;
            }
            beforeStart();
            setState(ServiceState.STARTING);
            doStart();
            setState(ServiceState.RUNNING);
            afterStart();
        } catch (Exception exception) {
            setState(ServiceState.START_FAILED);
            throw new ServiceException.StartException(exception);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void stop() {
        final Lock lock = getLock();
        lock.lock();
        try {
            if (isRunning()) {
                setState(ServiceState.STOPPING);
                try {
                    doStop();
                } catch (Exception ignored) {
                }
                setState(ServiceState.STOPPED);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 启动之前的校验及准备工作
     *
     * @throws Exception 校验异常
     */
    protected void beforeStart() throws Exception {
    }

    protected abstract void doStart() throws Exception;

    /**
     * 为多线程准备的, 只有在aft
     */
    protected void afterStart() {
    }

    protected abstract void doStop() throws Exception;
}
