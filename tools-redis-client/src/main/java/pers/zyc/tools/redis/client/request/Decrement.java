package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class Decrement extends Request {

	public Decrement(String key) {
		super(
				Util.toByteArray(key)
		);
	}

	@Override
	protected String getCommand() {
		return "DECR";
	}
}
