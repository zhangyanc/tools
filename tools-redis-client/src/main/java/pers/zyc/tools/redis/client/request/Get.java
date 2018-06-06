package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;
import pers.zyc.tools.redis.client.Request;

/**
 * @author zhangyancheng
 */
public class Get extends Request {

	public Get(String key) {
		super(
				Protocol.toByteArray(key)
		);
	}
}
