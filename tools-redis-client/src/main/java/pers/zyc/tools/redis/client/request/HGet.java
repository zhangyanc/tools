package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class HGet extends Request {

	public HGet(String key, String field) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(field)
		);
	}
}
