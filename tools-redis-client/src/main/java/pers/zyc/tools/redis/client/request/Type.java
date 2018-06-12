package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class Type extends Request {

	public Type(String key) {
		super(
				Util.toByteArray(key)
		);
	}
}
