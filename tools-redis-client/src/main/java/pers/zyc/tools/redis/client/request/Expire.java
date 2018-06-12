package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class Expire extends Request {

	public Expire(String key, int seconds) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(seconds)
		);
	}
}
