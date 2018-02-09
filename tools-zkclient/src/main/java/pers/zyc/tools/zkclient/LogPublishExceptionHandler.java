package pers.zyc.tools.zkclient;

import org.slf4j.Logger;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.event.PublishExceptionHandler;

import java.util.Objects;

/**
 * log 发布异常处理器
 *
 * @author zhangyancheng
 */
public class LogPublishExceptionHandler implements PublishExceptionHandler {
    private final Logger logger;

    public LogPublishExceptionHandler(Logger logger) {
        this.logger = Objects.requireNonNull(logger);
    }

    @Override
    public void handleException(Exception e, Object event, EventListener eventListener) {
        logger.error("Event publish error!", e);
    }
}
