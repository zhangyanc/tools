package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class SetBit extends Request {

	public SetBit(String key, long offset, boolean value) {
		super(Protocol.toByteArray(key), Protocol.toByteArray(offset), Protocol.toByteArray(value));
	}

	public SetBit(String key, long offset, String value) {
		super(Protocol.toByteArray(key), Protocol.toByteArray(offset), Protocol.toByteArray(value));
	}
}
