package pers.zyc.tools.event;

/**
 * 同步串行发布(在publisher调用线程中发布事件)
 *
 * @author zhangyancheng
 */
public class SerialEventPublisher extends BaseEventPublisher {

    public SerialEventPublisher() {
    }

    public SerialEventPublisher(PublishExceptionHandler publishExceptionHandler) {
        setPublishExceptionHandler(publishExceptionHandler);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPublish(Object event, EventListener listener) throws Exception {
        listener.onEvent(event);
    }
}
