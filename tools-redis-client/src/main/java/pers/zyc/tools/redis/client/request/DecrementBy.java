package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.Util;

/**
 * @author zhangyancheng
 */
public class DecrementBy extends Request {

	public DecrementBy(String key, long integer) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(integer)
		);
	}

	@Override
	protected String getCommand() {
		return "DECRBY";
	}
}
