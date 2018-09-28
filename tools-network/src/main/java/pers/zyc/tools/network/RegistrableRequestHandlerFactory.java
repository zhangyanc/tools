package pers.zyc.tools.network;

import java.util.HashMap;
import java.util.Map;

/**
 * 可注册请求处理器工厂
 *
 * @author zhangyancheng
 */
public class RegistrableRequestHandlerFactory implements RequestHandlerFactory {

	/**
	 * 请求处理器Map
	 */
	private final Map<Integer, RequestHandler> requestHandlerMap = new HashMap<>();

	@Override
	public RequestHandler getHandler(int requestType) {
		return requestHandlerMap.get(requestType);
	}

	/**
	 * 注册请求处理器
	 *
	 * @param requestType 请求类型
	 * @param requestHandler 请求处理器
	 */
	public void register(int requestType, RequestHandler requestHandler) {
		requestHandlerMap.put(requestType, requestHandler);
	}

	/**
	 * 注册单一命令请求处理器
	 *
	 * @param requestHandler 单一命令请求处理器
	 */
	public void register(SingleRequestSupportRequestHandler<? extends Request> requestHandler) {
		register(requestHandler.supportedRequestType(), requestHandler);
	}
}
