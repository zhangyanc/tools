package pers.zyc.tools.event;

/**
 * <code>onEvent</code> 异常处理器
 *
 * @author zhangyancheng
 */
public interface PublishExceptionHandler {

    void handleException(Exception e, Object event, EventListener eventListener);


    PublishExceptionHandler IGNORE_HANDLER = new PublishExceptionHandler() {

        @Override
        public void handleException(Exception e, Object event, EventListener eventListener) {
        }
    };

}
