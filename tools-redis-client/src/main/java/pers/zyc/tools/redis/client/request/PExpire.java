package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;
import pers.zyc.tools.redis.client.Request;

/**
 * @author zhangyancheng
 */
public class PExpire extends Request {

	public PExpire(String key, long milliseconds) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(milliseconds)
		);
	}
}