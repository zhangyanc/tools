package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class IncrementBy extends Request {

	public IncrementBy(String key, long integer) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(integer)
		);
	}

	@Override
	protected String getCommand() {
		return "INCRBY";
	}
}
