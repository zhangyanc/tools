package pers.zyc.tools.redis.client;

/**
 * @author zhangyancheng
 */
class RequestAction<R> {

	private Request request;
	private Connection connection;
	private ResponseCast<R> responseCast;

	RequestAction<R> request(Request request) {
		this.request = request;
		return this;
	}

	RequestAction<R> connection(Connection connection) {
		this.connection = connection;
		return this;
	}

	RequestAction<R> responseCast(ResponseCast<R> responseCast) {
		this.responseCast = responseCast;
		return this;
	}

	ResponseFuture<R> execute() {
		connection.sendRequest(request);
		return new ResponseFuture<>(connection, responseCast);
	}
}
