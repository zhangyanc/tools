package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class DecrementBy extends Request {

	public DecrementBy(String key, long integer) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(integer)
		);
	}

	@Override
	protected String getCommand() {
		return "DECRBY";
	}
}
