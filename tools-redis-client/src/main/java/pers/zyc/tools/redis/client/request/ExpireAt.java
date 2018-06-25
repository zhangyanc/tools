package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class ExpireAt extends Request {

	public ExpireAt(String key, long unixTime) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(unixTime)
		);
	}
}
