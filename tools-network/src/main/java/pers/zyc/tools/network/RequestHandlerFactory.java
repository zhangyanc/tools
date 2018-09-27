package pers.zyc.tools.network;

/**
 * 请求处理器工厂
 *
 * @author zhangyancheng
 */
public interface RequestHandlerFactory {

	/**
	 * 通过请求类型获取请求处理器
	 *
	 * @param requestType 请求类型
	 * @return 请求处理器
	 */
	RequestHandler getHandler(int requestType);
}
