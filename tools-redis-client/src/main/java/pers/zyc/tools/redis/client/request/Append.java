package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class Append extends Request {

	public Append(String key, String value) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(value)
		);
	}
}
