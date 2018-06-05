package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class SetRange extends Request {

	public SetRange(String key, long offset, String value) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(offset),
				Protocol.toByteArray(value)
		);
	}
}
