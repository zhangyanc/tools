package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class PExpire extends Request {

	public PExpire(String key, long milliseconds) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(milliseconds)
		);
	}
}
