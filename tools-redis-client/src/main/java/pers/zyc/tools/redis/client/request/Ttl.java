package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class Ttl extends Request {

	public Ttl(String key) {
		super(
				Protocol.toByteArray(key)
		);
	}
}
