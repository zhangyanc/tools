package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class SetEx extends Request {

	public SetEx(String key, int seconds, String value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(seconds),
				ByteUtil.toByteArray(value)
		);
	}
}
