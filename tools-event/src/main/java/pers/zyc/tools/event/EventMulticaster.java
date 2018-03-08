package pers.zyc.tools.event;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;

/**
 * 事件广播器
 * @author zhangyancheng
 */
public final class EventMulticaster<L extends EventListener> implements Listenable<L> {

    /**
     * 同步执行器, 在multicaster调用线程中执行广播
     */
    private static final Executor SYNC_EXECUTOR = new Executor() {
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    };

    /**
     * 代理, 完成事件广播
     */
    private final L multicaster;

    /**
     * 广播异常处理器
     */
    private MulticastExceptionHandler exceptionHandler;

    /**
     * 广播执行器
     */
    private Executor multicastExecutor = SYNC_EXECUTOR;
    /**
     * 监听器set, 同一个监听器不会重复添加
     */
    private Set<EventListener> eventListeners = new CopyOnWriteArraySet<>();

    public EventMulticaster(Class<L> listenerClass) {
        this(listenerClass, null, null);
    }

    public EventMulticaster(Class<L> listenerClass,
                            Executor multicastExecutor) {

        this(listenerClass, multicastExecutor, null);
    }

    public EventMulticaster(Class<L> listenerClass,
                            MulticastExceptionHandler exceptionHandle) {

        this(listenerClass, null, exceptionHandle);
    }

    public EventMulticaster(Class<L> listenerClass,
                            Executor multicastExecutor,
                            MulticastExceptionHandler exceptionHandle) {

        Objects.requireNonNull(listenerClass);
        //multicastExecutor不能为空
        if (multicastExecutor != null) {
            this.multicastExecutor = multicastExecutor;
        }
        //exceptionHandler可以为空, 为空则不处理回调异常
        this.exceptionHandler = exceptionHandle;

        //创建针对监听器接口的代理对象
        Object proxy = Proxy.newProxyInstance(listenerClass.getClassLoader(),
                new Class[] { listenerClass }, new MulticastInvocationHandler());
        this.multicaster = listenerClass.cast(proxy);
    }

    @Override
    public void addListener(L listener) {
        eventListeners.add(Objects.requireNonNull(listener));
    }

    @Override
    public void removeListener(L listener) {
        eventListeners.remove(listener);
    }

    /**
     * 清空已添加的所有监听器
     */
    public void removeAllListeners() {
        eventListeners.clear();
    }

    /**
     * 返回泛型接口的代理实例, 在代理实例上调用事件, 则将此调用广播给所有已添加的监听器
     * @return 代理实例
     */
    public L cast() {
        return multicaster;
    }

    private class MulticastInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
            //回调所有监听器
            for (final EventListener listener : eventListeners) {
                multicastExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            method.invoke(listener, args);
                        } catch (Throwable throwable) {
                            if (exceptionHandler != null) {
                                if (throwable instanceof InvocationTargetException) {
                                    //保留反射调用的原始异常
                                    throwable = ((InvocationTargetException) throwable).getTargetException();
                                }
                                try {
                                    exceptionHandler.handleException(throwable, listener, method, args);
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                });
            }
            return null;
        }
    }

    public MulticastExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * 设置监听器回调异常处理器
     * @param exceptionHandler 监听器回调异常处理器
     */
    public void setExceptionHandler(MulticastExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * @return 广播执行器
     */
    public Executor getMulticastExecutor() {
        return multicastExecutor;
    }

    /**
     * 设置广播执行器, 如果为ExecutorService类型则广播具备并行能力
     * @param multicastExecutor 广播执行器
     * @throws NullPointerException multicastExecutor时抛出
     * @see java.util.concurrent.ExecutorService
     */
    public void setMulticastExecutor(Executor multicastExecutor) {
        this.multicastExecutor = Objects.requireNonNull(multicastExecutor);
    }
}
