package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.Util;

/**
 * @author zhangyancheng
 */
public class GetBit extends Request {

	public GetBit(String key, long offset) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(offset)
		);
	}
}
