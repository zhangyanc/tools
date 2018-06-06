package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;
import pers.zyc.tools.redis.client.Request;

/**
 * @author zhangyancheng
 */
public class DecrementBy extends Request {

	public DecrementBy(String key, long integer) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(integer)
		);
	}

	@Override
	protected String getCommand() {
		return "DECRBY";
	}
}
