package pers.zyc.tools.lifecycle;

import pers.zyc.tools.utils.Locker;
import pers.zyc.tools.utils.Stateful;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 抽象服务类
 *
 * @author zhangyancheng
 */
public abstract class Service implements Lifecycle, Locker<Lock>, Stateful<ServiceState> {

    /**
     * 服务状态
     */
    private volatile ServiceState serviceState = ServiceState.NEW;
    /**
     * 服务锁
     */
    private final Lock serviceLock = Objects.requireNonNull(initServiceLock());

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
     * 初始化锁, 子类重写时需注意此方法在子类构造方法前调用
     */
    protected Lock initServiceLock() {
        return new ReentrantLock();
    }

    /**
     * @return 服务名
     */
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * 检查服务是否处于RUNNING状态, 不是则抛出异常
     *
     * @throws ServiceException.NotRunningException if service not running
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

    /**
     * 启动服务
     *
     * @throws ServiceException.StartException 启动异常
     */
    @Override
    public void start() {
        final Lock lock = getLock();
        lock.lock();
        try {
            //只有在非运行状态才启动服务
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

    /**
     * 停止服务
     */
    @Override
    public void stop() {
        final Lock lock = getLock();
        lock.lock();
        try {
            //只有在运行状态才停止服务
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

    /**
     * 子类需要重写的具体启动逻辑
     *
     * @throws Exception 启动异常
     */
    protected abstract void doStart() throws Exception;

    /**
     * 只有在afterStart中启动的线程才能保证读到RUNNING状态
     */
    protected void afterStart() {
    }

    /**
     * 子类需要重写的具体停止逻辑
     *
     * @throws Exception 停止异常
     */
    protected abstract void doStop() throws Exception;
}
