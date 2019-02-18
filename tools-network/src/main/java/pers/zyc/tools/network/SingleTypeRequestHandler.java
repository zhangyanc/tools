package pers.zyc.tools.network;

/**
 * 单一类型请求处理器
 *
 * @author zhangyancheng
 */
public abstract class SingleTypeRequestHandler<R extends Request> extends BaseRequestHandler {

	/**
	 * @return 处理器唯一支持的请求类型
	 */
	public abstract int supportedRequestType();

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
