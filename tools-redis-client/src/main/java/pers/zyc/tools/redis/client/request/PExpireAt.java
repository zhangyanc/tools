package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.Util;

/**
 * @author zhangyancheng
 */
public class PExpireAt extends Request {

	public PExpireAt(String key, long millisecondsTimestamp) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(millisecondsTimestamp)
		);
	}
}
