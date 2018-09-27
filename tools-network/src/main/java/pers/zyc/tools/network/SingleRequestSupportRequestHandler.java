package pers.zyc.tools.network;

import java.util.concurrent.Executor;

/**
 * @author zhangyancheng
 */
public abstract class SingleRequestSupportRequestHandler<R extends Request> implements RequestHandler {

	private Executor executor;

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

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

	protected abstract Response handle0(R request);
}
