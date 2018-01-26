package pers.zyc.tools.event;

/**
 * <code>onEvent</code> 异常处理器
 *
 * @author zhangyancheng
 */
public interface ExceptionHandler {

    void onInvokeException(Exception e, Object event, EventListener eventListener);


    ExceptionHandler IGNORE_HANDLER = new ExceptionHandler() {

        @Override
        public void onInvokeException(Exception e, Object event, EventListener eventListener) {
        }
    };

}
