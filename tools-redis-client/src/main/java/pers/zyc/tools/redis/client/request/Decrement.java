package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;
import pers.zyc.tools.redis.client.Request;

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
	protected String getCommand() {
		return "DECR";
	}
}
