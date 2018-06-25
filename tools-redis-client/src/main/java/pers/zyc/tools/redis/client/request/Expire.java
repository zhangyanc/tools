package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class Expire extends Request {

	public Expire(String key, int seconds) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(seconds)
		);
	}
}
