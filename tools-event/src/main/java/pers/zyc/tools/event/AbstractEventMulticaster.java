package pers.zyc.tools.event;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author zhangyancheng
 */
public abstract class AbstractEventMulticaster implements EventMulticaster {
    /**
     * 监听器set集合, 同一个监听器不会重复添加
     */
    private Set<EventListener> eventListeners = new CopyOnWriteArraySet<>();
    private MulticastExceptionHandler exceptionHandler;

    @Override
    public void addListener(EventListener listener) {
        eventListeners.add(Objects.requireNonNull(listener));
    }

    @Override
    public void removeListener(EventListener listener) {
        eventListeners.remove(listener);
    }

    @Override
    public void removeAllListeners() {
        eventListeners.clear();
    }

    @Override
    public void setDeliverExceptionHandler(MulticastExceptionHandler multicastExceptionHandler) {
        this.exceptionHandler = multicastExceptionHandler;
    }

    @Override
    public void multicastEvent(Object event) {
        for (EventListener listener : eventListeners) {
            multicast(event, listener);
        }
    }

    @SuppressWarnings("unchecked")
    private void multicast(final Object event, final EventListener eventListener) {
        getMulticastExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //回调
                    eventListener.onEvent(event);
                } catch (Exception e) {
                    //异常处理
                    if (exceptionHandler != null) {
                        exceptionHandler.handleException(e, event, eventListener);
                    }
                }
            }
        });
    }
}
