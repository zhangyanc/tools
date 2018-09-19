package pers.zyc.tools.network;

import java.util.concurrent.Executor;

/**
 * @author zhangyancheng
 */
public interface RequestHandler {

	Executor getExecutor();

	Response handle(Request request);
}
