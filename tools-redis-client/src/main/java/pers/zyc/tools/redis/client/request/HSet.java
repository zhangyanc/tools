package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.Util;

/**
 * @author zhangyancheng
 */
public class HSet extends Request {

	public HSet(String key, String field, String value) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(field),
				Util.toByteArray(value)
		);
	}
}
