package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class Ttl extends Request {

	public Ttl(String key) {
		super(
				Util.toByteArray(key)
		);
	}
}
