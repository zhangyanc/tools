package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class StrLen extends Request {

	public StrLen(String key) {
		super(
				Util.toByteArray(key)
		);
	}
}
