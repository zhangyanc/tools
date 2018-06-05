package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

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
