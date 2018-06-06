package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;
import pers.zyc.tools.redis.client.Request;

/**
 * @author zhangyancheng
 */
public class Increment extends Request {

	public Increment(String key) {
		super(
				Protocol.toByteArray(key)
		);
	}

	@Override
	protected String getCommand() {
		return "INCR";
	}
}
