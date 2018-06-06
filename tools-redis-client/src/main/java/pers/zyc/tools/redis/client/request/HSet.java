package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;
import pers.zyc.tools.redis.client.Request;

/**
 * @author zhangyancheng
 */
public class HSet extends Request {

	public HSet(String key, String field, String value) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(field),
				Protocol.toByteArray(value)
		);
	}
}
