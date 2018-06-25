package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class IncrementByFloat extends Request {

	public IncrementByFloat(String key, double value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(value)
		);
	}

	@Override
	protected String getCommand() {
		return "INCRBYFLOAT";
	}
}
