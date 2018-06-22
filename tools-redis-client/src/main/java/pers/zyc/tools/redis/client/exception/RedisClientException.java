package pers.zyc.tools.redis.client.exception;

/**
 * @author zhangyancheng
 */
public class RedisClientException extends RuntimeException {

	public RedisClientException(String message) {
		super(message);
	}

	public RedisClientException(Throwable cause) {
		super(cause);
	}

	public RedisClientException(String message, Throwable cause) {
		super(message, cause);
	}
}
