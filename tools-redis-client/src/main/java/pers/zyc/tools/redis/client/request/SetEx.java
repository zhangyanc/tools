package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class SetEx extends Request {

	public SetEx(String key, int seconds, String value) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(seconds),
				Util.toByteArray(value)
		);
	}
}
