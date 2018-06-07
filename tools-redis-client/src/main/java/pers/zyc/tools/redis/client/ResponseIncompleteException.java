package pers.zyc.tools.redis.client;

/**
 * @author zhangyancheng
 */
class ResponseIncompleteException extends RedisClientException {

	ResponseIncompleteException(String message) {
		super(message);
	}
}
