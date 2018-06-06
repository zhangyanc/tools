package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;
import pers.zyc.tools.redis.client.Request;

/**
 * @author zhangyancheng
 */
public class GetSet extends Request {

	public GetSet(String key, String value) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(value)
		);
	}
}
