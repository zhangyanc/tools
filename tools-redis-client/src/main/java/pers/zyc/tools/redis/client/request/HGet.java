package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class HGet extends Request {

	public HGet(String key, String field) {
		super(Protocol.toByteArray(key), Protocol.toByteArray(field));
	}
}
