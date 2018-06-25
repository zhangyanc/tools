package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.Util;

/**
 * @author zhangyancheng
 */
public class SetRange extends Request {

	public SetRange(String key, long offset, String value) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(offset),
				Util.toByteArray(value)
		);
	}
}
