package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class SetBit extends Request {

	public SetBit(String key, long offset, boolean value) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(offset),
				Util.toByteArray(value)
		);
	}

	public SetBit(String key, long offset, String value) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(offset),
				Util.toByteArray(value)
		);
	}
}
