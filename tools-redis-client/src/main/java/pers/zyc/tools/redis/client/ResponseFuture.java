package pers.zyc.tools.redis.client;

/**
 * @author zhangyancheng
 */
public class ResponseFuture<R> {

	private final Connection connection;

	ResponseFuture(Connection connection) {
		this.connection = connection;
	}

	@SuppressWarnings("unchecked")
	public R get(long timeout) {
		return (R) connection.getResponse(timeout);
	}
}
