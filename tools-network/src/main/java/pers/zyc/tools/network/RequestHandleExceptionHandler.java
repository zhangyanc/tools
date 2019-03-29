package pers.zyc.tools.network;

import pers.zyc.tools.utils.ExceptionHandler;

/**
 * @author zhangyancheng
 */
public interface RequestHandleExceptionHandler extends ExceptionHandler<Exception, Request, Response> {

	/**
	 * 异常处理
	 *
	 * @param cause 异常
	 * @param request 请求
	 * @return 响应
	 */
	@Override
	Response handleException(Exception cause, Request request);
}
