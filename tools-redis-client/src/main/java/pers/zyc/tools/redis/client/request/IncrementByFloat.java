package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class IncrementByFloat extends Request {

	public IncrementByFloat(String key, double value) {
		super(Protocol.toByteArray(key), Protocol.toByteArray(value));
	}

	@Override
	String getCommand() {
		return "INCRBYFLOAT";
	}
}
