package pers.zyc.tools.network;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangyancheng
 */
public class RegistrableRequestHandlerFactory implements RequestHandlerFactory {

	private final Map<Integer, RequestHandler> requestHandlerMap = new HashMap<>();

	@Override
	public RequestHandler getHandler(int requestType) {
		return requestHandlerMap.get(requestType);
	}

	public void register(int requestType, RequestHandler requestHandler) {
		requestHandlerMap.put(requestType, requestHandler);
	}

	public void register(SingleRequestSupportRequestHandler<? extends Request> requestHandler) {
		register(requestHandler.supportedRequestType(), requestHandler);
	}
}
