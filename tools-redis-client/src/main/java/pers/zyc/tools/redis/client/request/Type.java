package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class Type extends Request {

	public Type(String key) {
		super(Protocol.toByteArray(key));
	}
}
