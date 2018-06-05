package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class PExpireAt extends Request {

	public PExpireAt(String key, long millisecondsTimestamp) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(millisecondsTimestamp)
		);
	}
}
