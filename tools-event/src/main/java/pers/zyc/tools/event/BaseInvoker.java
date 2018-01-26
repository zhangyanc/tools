package pers.zyc.tools.event;

import java.util.Collection;
import java.util.Objects;

/**
 * @author zhangyancheng
 */
public abstract class BaseInvoker implements ListenerInvoker {

    private ExceptionHandler exceptionHandler = ExceptionHandler.IGNORE_HANDLER;

    @Override
    public <E> void invoke(E event, EventListener<E> listener) {
        try {
            doInvoke(event, listener);
        } catch (Exception e) {
            exceptionHandler.onInvokeException(e, event, listener);
        }
    }

    protected abstract void doInvoke(Object event, EventListener listener) throws Exception;

    @Override
    public <E> void invoke(E event, Collection<EventListener<E>> eventListeners) {
        for (EventListener<E> listener : eventListeners) {
            invoke(event, listener);
        }
    }

    @Override
    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler);
    }
}
