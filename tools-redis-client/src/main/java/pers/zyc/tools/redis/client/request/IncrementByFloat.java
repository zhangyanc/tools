package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;
import pers.zyc.tools.redis.client.Request;

/**
 * @author zhangyancheng
 */
public class IncrementByFloat extends Request {

	public IncrementByFloat(String key, double value) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(value)
		);
	}

	@Override
	protected String getCommand() {
		return "INCRBYFLOAT";
	}
}
