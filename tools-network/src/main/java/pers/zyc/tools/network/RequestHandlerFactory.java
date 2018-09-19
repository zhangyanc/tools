package pers.zyc.tools.network;

/**
 * @author zhangyancheng
 */
public interface RequestHandlerFactory {

	RequestHandler getHandler(int requestType);
}
