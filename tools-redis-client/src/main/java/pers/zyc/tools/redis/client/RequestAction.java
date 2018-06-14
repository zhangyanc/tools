package pers.zyc.tools.redis.client;

import java.util.Objects;

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

	private void validate() {
		Objects.requireNonNull(request);
		Objects.requireNonNull(connection);
		Objects.requireNonNull(responseCast);
	}

	ResponseFuture<R> execute() {
		validate();

		connection.sendRequest(request);

		return new ResponseFuture<R>() {

			@Override
			public R get() {
				return responseCast.cast(connection.getResponse());
			}
		};
	}
}
