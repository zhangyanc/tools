package pers.zyc.tools.zkclient;

import org.slf4j.Logger;
import pers.zyc.tools.event.Listener;
import pers.zyc.tools.event.MulticastExceptionHandler;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * log 发布异常处理器
 *
 * @author zhangyancheng
 */
public class LogMulticastExceptionHandler implements MulticastExceptionHandler {
    private final Logger logger;

    public LogMulticastExceptionHandler(Logger logger) {
        this.logger = Objects.requireNonNull(logger);
    }

    @Override
    public void handleException(Throwable throwable,
                                Listener eventListener,
                                Method method,
                                Object[] args) throws Exception {

        logger.error("Event multicast error: {}, {}|{}", throwable, eventListener, method);
    }
}
