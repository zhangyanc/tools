package pers.zyc.tools.zkclient;

import org.slf4j.Logger;
import pers.zyc.tools.event.Event;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.event.DeliverExceptionHandler;

import java.util.Objects;

/**
 * log 发布异常处理器
 *
 * @author zhangyancheng
 */
public class LogDeliverExceptionHandler implements DeliverExceptionHandler {
    private final Logger logger;

    public LogDeliverExceptionHandler(Logger logger) {
        this.logger = Objects.requireNonNull(logger);
    }

    @Override
    public void handleException(Exception e, Event event, EventListener eventListener) {
        logger.error("Event publish error!", e);
    }
}
