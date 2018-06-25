package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.Util;

/**
 * @author zhangyancheng
 */
public class HIncrementByFloat extends Request {

	public HIncrementByFloat(String key, String field, double value) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(field),
				Util.toByteArray(value)
		);
	}

	@Override
	protected String getCommand() {
		return "HINCRBYFLOAT";
	}
}
