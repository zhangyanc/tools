package pers.zyc.tools.utils.event;

import pers.zyc.tools.utils.ExceptionHandler;

/**
 * 广播异常处理器
 *
 * @author zhangyancheng
 */
public interface MulticastExceptionHandler extends ExceptionHandler<Throwable, MulticastDetail, Void> {

	/**
	 * 处理广播异常
	 *
	 * @param cause 广播异常
	 * @param multicastDetail 广播调用信息
	 */
	Void handleException(Throwable cause, MulticastDetail multicastDetail);
}
