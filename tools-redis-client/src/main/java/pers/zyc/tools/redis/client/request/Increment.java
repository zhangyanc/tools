package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class Increment extends Request {

	public Increment(String key) {
		super(Protocol.toByteArray(key));
	}

	@Override
	String getCommand() {
		return "INCR";
	}
}
