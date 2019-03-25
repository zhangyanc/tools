package pers.zyc.tools.network;

import java.util.concurrent.Executor;

/**
 * 请求处理器
 *
 * @author zhangyancheng
 */
public interface RequestHandler {

	/**
	 * @return 请求处理执行器
	 */
	Executor getExecutor();

	/**
	 * 处理请求
	 *
	 * @param request 请求
	 * @return 响应
	 */
	Response handle(Request request) throws Exception;
}
