package pers.zyc.tools.event;

import java.util.Collection;
import java.util.Objects;

/**
 * @author zhangyancheng
 */
public abstract class BaseEventPublisher implements EventPublisher {

    private PublishExceptionHandler publishExceptionHandler = PublishExceptionHandler.IGNORE_HANDLER;

    @Override
    public <E> void publish(E event, EventListener<E> listener) {
        try {
            doPublish(event, listener);
        } catch (Exception e) {
            publishExceptionHandler.handleException(e, event, listener);
        }
    }

    protected abstract void doPublish(Object event, EventListener listener) throws Exception;

    @Override
    public <E> void publish(E event, Collection<EventListener<E>> eventListeners) {
        for (EventListener<E> listener : eventListeners) {
            publish(event, listener);
        }
    }

    @Override
    public void setPublishExceptionHandler(PublishExceptionHandler publishExceptionHandler) {
        this.publishExceptionHandler = Objects.requireNonNull(publishExceptionHandler);
    }
}
