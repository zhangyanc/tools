package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;
import pers.zyc.tools.redis.client.Request;

/**
 * @author zhangyancheng
 */
public class Expire extends Request {

	public Expire(String key, int seconds) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(seconds)
		);
	}
}
