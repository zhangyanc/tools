package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class IncrementBy extends Request {

	public IncrementBy(String key, long integer) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(integer)
		);
	}

	@Override
	String getCommand() {
		return "INCRBY";
	}
}
