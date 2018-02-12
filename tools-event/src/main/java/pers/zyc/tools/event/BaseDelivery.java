package pers.zyc.tools.event;

import java.util.Collection;

/**
 * @author zhangyancheng
 */
public abstract class BaseDelivery implements EventDelivery {

    private DeliverExceptionHandler exceptionHandler;

    @Override
    public <E extends Event> void deliver(final E event, final EventListener<E> eventListener) {
        getDeliverTaskExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onEvent(event);
                } catch (Exception exception) {
                    if (exceptionHandler != null) {
                        exceptionHandler.handleException(exception, event, eventListener);
                    }
                }
            }
        });
    }

    @Override
    public <E extends Event> void deliver(E event, Collection<EventListener<E>> eventListeners) {
        for (EventListener listener : eventListeners) {
            deliver(event, listener);
        }
    }

    @Override
    public void setDeliverExceptionHandler(DeliverExceptionHandler deliverExceptionHandler) {
        this.exceptionHandler = deliverExceptionHandler;
    }
}
