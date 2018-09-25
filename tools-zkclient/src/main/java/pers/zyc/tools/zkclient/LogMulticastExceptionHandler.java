package pers.zyc.tools.zkclient;

import org.slf4j.Logger;
import pers.zyc.tools.utils.event.MulticastDetail;
import pers.zyc.tools.utils.event.MulticastExceptionHandler;

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
	public Void handleException(Throwable throwable, MulticastDetail multicastDetail) {
		logger.error("Multicast error: " + multicastDetail, throwable);
		return null;
	}
}
