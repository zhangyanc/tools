package pers.zyc.retry;

import pers.zyc.retry.exception.RetryCanceledException;
import pers.zyc.retry.exception.RetryFailedException;
import pers.zyc.retry.exception.RunOutOfPolicyException;
import pers.zyc.tools.utils.Stateful;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 重试任务
 *
 * @author zhangyancheng
 */
public abstract class RetryAble<R> implements Callable<R>, Stateful<State> {

    private final R defaultResult;//任务默认值

    private AtomicReference<State> state = new AtomicReference<>(State.RETRYING);//simple state control

    public RetryAble() {
        this.defaultResult = null;
    }

    public RetryAble(R defaultResult) {
        this.defaultResult = defaultResult;
    }

    @Override
    public State getState() {
        return state.get();
    }

    @Override
    public void setState(State state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean checkState(State state) {
        return this.state.get().equals(state);
    }

    boolean isNotCanceled() {
        return !checkState(State.CANCELED);
    }

    /**
     * @return 当且仅当RETRYING状态取消
     */
    public boolean cancel() {
        if (state.compareAndSet(State.RETRYING, State.CANCELED)) {
            synchronized (this) {
                this.notify();
            }
            return true;
        }
        return false;
    }

    @Override
    public R call() throws Exception {
        R ret = doRetry();
        if (!state.compareAndSet(State.RETRYING, State.SUCCESS)) {
            throw new RetryCanceledException();
        }
        return ret;
    }

    /**
     * 要重试的逻辑, 此方法只有抛出异常后才会重试, 但不包括线程打断异常
     *
     * @return result
     * @throws Exception exception
     */
    protected abstract R doRetry() throws Exception;

    /**
     * @param exception retry exception
     * @return 是否继续重试
     */
    protected boolean onException(Exception exception) {
        return true;
    }

    /**
     * 重试失败了, 需要检查是否返回默认值
     *
     * @throws RetryFailedException 不存在默认值时抛出
     */
    protected R checkDefault() throws RetryFailedException {
        if (!state.compareAndSet(State.RETRYING, State.FAILED)) {
            throw new RetryCanceledException();
        }
        if (defaultResult == null) {
            throw new RunOutOfPolicyException();
        }
        return defaultResult;
    }

    /**
     * 延迟到重试时间或者被取消重试
     *
     * @param time policy 给出的下次重试时间
     * @throws InterruptedException 线程被中断
     */
    protected void waitUntil(long time) throws InterruptedException {
        synchronized (this) {
            long now;
            while ((now = System.currentTimeMillis()) < time && isNotCanceled()) {
                this.wait(time - now);
            }
        }
    }
}
