package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class IncrementBy extends Request {

	public IncrementBy(String key, long integer) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(integer)
		);
	}

	@Override
	protected String getCommand() {
		return "INCRBY";
	}
}
