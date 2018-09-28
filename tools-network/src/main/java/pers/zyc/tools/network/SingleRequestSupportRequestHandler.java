package pers.zyc.tools.network;

import java.util.concurrent.Executor;

/**
 * 单一命令请求处理器
 *
 * @author zhangyancheng
 */
public abstract class SingleRequestSupportRequestHandler<R extends Request> implements RequestHandler {

	/**
	 * 请求处理执行器
	 */
	private Executor executor;

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	/**
	 * @return 处理器支持的请求类型
	 */
	public abstract int supportedRequestType();

	@Override
	public Executor getExecutor() {
		return executor;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response handle(Request request) {
		return handle0((R) request);
	}

	/**
	 * 处理请求
	 *
	 * @param request 请求
	 * @return 响应
	 */
	protected abstract Response handle0(R request);
}
