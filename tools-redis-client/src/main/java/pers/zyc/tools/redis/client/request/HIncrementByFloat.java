package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class HIncrementByFloat extends Request {

	public HIncrementByFloat(String key, String field, double value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(field),
				ByteUtil.toByteArray(value)
		);
	}

	@Override
	protected String getCommand() {
		return "HINCRBYFLOAT";
	}
}
