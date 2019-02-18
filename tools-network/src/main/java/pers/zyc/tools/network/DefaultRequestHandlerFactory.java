package pers.zyc.tools.network;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认请求处理器工厂
 *
 * @author zhangyancheng
 */
public class DefaultRequestHandlerFactory implements RequestHandlerFactory {

	/**
	 * 请求处理器Map
	 */
	private final Map<Integer, RequestHandler> requestHandlerMap = new HashMap<>();

	@Override
	public RequestHandler getHandler(int requestType) {
		return requestHandlerMap.get(requestType);
	}

	/**
	 * 添加请求处理器
	 *
	 * @param requestHandler 请求处理器
	 * @param requestTypes 请求类型
	 */
	public void addHandler(RequestHandler requestHandler, int... requestTypes) {
		for (int requestType : requestTypes) {
			requestHandlerMap.put(requestType, requestHandler);
		}
	}

	/**
	 * 添加单一类型请求处理器
	 *
	 * @param singleTypeRequestHandlers 单一命令请求处理器
	 */
	public void addHandler(SingleTypeRequestHandler... singleTypeRequestHandlers) {
		for (SingleTypeRequestHandler requestHandler : singleTypeRequestHandlers) {
			addHandler(requestHandler, requestHandler.supportedRequestType());
		}
	}
}
