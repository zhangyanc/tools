package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.Util;

/**
 * @author zhangyancheng
 */
public class IncrementByFloat extends Request {

	public IncrementByFloat(String key, double value) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(value)
		);
	}

	@Override
	protected String getCommand() {
		return "INCRBYFLOAT";
	}
}
