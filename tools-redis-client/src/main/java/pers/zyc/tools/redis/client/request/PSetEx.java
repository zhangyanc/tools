package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.Util;

/**
 * @author zhangyancheng
 */
public class PSetEx extends Request {

	public PSetEx(String key, long milliseconds, String value) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(milliseconds),
				Util.toByteArray(value)
		);
	}
}
