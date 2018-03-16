package pers.zyc.tools.zkclient;

import org.slf4j.Logger;
import pers.zyc.tools.event.MulticastDetail;
import pers.zyc.tools.event.MulticastExceptionHandler;

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
		logger.error("Event multicast error: {}, {}|{}", throwable, multicastDetail.listener, multicastDetail.method);
		return null;
	}
}
