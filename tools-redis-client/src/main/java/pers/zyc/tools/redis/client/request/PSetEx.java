package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class PSetEx extends Request {

	public PSetEx(String key, long milliseconds, String value) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(milliseconds),
				Protocol.toByteArray(value)
		);
	}
}
