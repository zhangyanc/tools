package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class SetEx extends Request {

	public SetEx(String key, int seconds, String value) {
		super(Protocol.toByteArray(key), Protocol.toByteArray(seconds), Protocol.toByteArray(value));
	}
}
