package pers.zyc.tools.network;

import pers.zyc.tools.utils.event.Listener;

/**
 * 响应监听器
 *
 * @author zhangyancheng
 */
public interface ResponseFutureListener extends Listener {

	/**
	 * 请求成功，接受响应
	 *
	 * @param request 请求
	 * @param response 响应
	 */
	void responseReceived(Request request, Response response);

	/**
	 * 请求异常
	 *
	 * @param request 请求
	 * @param re 异常
	 */
	void exceptionCaught(Request request, NetworkException re);
}
