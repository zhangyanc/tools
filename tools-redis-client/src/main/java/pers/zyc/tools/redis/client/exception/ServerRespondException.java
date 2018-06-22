package pers.zyc.tools.redis.client.exception;

/**
 * @author zhangyancheng
 */
public class ServerRespondException extends RedisClientException {

	private final ErrorType errorType;

	public ServerRespondException(String message) {
		super(message);
		this.errorType = ErrorType.parse(message);
	}

	public ErrorType getErrorType() {
		return errorType;
	}
}
