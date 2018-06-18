package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class HExists extends Request {

	public HExists(String key, String field) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(field)
		);
	}
}
