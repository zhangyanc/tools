package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class Get extends Request {

	public Get(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}
}
