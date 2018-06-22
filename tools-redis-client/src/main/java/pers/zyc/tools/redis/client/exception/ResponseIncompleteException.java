package pers.zyc.tools.redis.client.exception;

/**
 * @author zhangyancheng
 */
public class ResponseIncompleteException extends RedisClientException {

	public ResponseIncompleteException(String message) {
		super(message);
	}
}
