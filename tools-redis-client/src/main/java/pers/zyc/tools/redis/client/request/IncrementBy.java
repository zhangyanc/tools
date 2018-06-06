package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;
import pers.zyc.tools.redis.client.Request;

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
	protected String getCommand() {
		return "INCRBY";
	}
}
