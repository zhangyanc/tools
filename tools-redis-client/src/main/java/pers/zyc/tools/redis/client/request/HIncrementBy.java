package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.Util;

/**
 * @author zhangyancheng
 */
public class HIncrementBy extends Request {

	public HIncrementBy(String key, String field, long value) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(field),
				Util.toByteArray(value)
		);
	}

	@Override
	protected String getCommand() {
		return "HINCRBY";
	}
}
