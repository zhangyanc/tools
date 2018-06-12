package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class ExpireAt extends Request {

	public ExpireAt(String key, long unixTime) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(unixTime)
		);
	}
}
