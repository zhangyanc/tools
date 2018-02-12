package pers.zyc.tools.event;

/**
 * <code>onEvent</code> 异常处理器
 *
 * @author zhangyancheng
 */
public interface DeliverExceptionHandler {

    void handleException(Exception exception, Event event, EventListener eventListener);
}
