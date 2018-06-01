package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class SetNx extends Request {

	public SetNx(String key, String value) {
		super(Protocol.toByteArray(key), Protocol.toByteArray(value));
	}
}
