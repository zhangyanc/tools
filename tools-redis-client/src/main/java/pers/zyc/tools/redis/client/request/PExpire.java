package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.Util;

/**
 * @author zhangyancheng
 */
public class PExpire extends Request {

	public PExpire(String key, long milliseconds) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(milliseconds)
		);
	}
}
