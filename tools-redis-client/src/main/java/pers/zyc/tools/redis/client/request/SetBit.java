package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class SetBit extends Request {

	public SetBit(String key, long offset, boolean value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(offset),
				ByteUtil.toByteArray(value)
		);
	}

	public SetBit(String key, long offset, String value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(offset),
				ByteUtil.toByteArray(value)
		);
	}
}
