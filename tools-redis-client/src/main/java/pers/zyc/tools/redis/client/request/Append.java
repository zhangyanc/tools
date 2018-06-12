package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class Append extends Request {

	public Append(String key, String value) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(value)
		);
	}
}
