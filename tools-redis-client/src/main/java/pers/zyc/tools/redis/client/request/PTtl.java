package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class PTtl extends Request {

	public PTtl(String key) {
		super(
				Protocol.toByteArray(key)
		);
	}
}
