package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class PSetEx extends Request {

	public PSetEx(String key, long milliseconds, String value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(milliseconds),
				ByteUtil.toByteArray(value)
		);
	}
}
