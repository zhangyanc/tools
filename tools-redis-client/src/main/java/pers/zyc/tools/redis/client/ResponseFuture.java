package pers.zyc.tools.redis.client;

/**
 * @author zhangyancheng
 */
public class ResponseFuture<R> {

	private final Connection connection;
	private final ResponseCast<R> responseCast;

	ResponseFuture(Connection connection, ResponseCast<R> responseCast) {
		this.connection = connection;
		this.responseCast = responseCast;
	}

	public R get(long timeout) {
		return responseCast.cast(connection.getResponse(timeout));
	}
}
