package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;
import pers.zyc.tools.redis.client.Request;

/**
 * @author zhangyancheng
 */
public class ExpireAt extends Request {

	public ExpireAt(String key, long unixTime) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(unixTime)
		);
	}
}
