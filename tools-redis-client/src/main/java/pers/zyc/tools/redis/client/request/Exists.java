package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class Exists extends Request {

	public Exists(String key) {
		super(
				Protocol.toByteArray(key)
		);
	}
}
