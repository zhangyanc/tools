package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class Decrement extends Request {

	public Decrement(String key) {
		super(
				Protocol.toByteArray(key)
		);
	}

	@Override
	String getCommand() {
		return "DECR";
	}
}
