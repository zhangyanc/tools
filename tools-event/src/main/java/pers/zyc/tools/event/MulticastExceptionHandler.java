package pers.zyc.tools.event;

import java.lang.reflect.Method;

/**
 * <code>onEvent</code> 异常处理器
 *
 * @author zhangyancheng
 */
public interface MulticastExceptionHandler {

    /**
     * 处理监听器回调异常
     *
     *@param throwable 监听器回调异常
     *@param eventListener 监听器
     *@param method 事件方法
     *@param args 方法参数
     *@throws Exception 处理异常
     */
    void handleException(Throwable throwable,
                         EventListener eventListener,
                         Method method,
                         Object[] args) throws Exception;
}
