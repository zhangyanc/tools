package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;
import pers.zyc.tools.redis.client.Request;

/**
 * @author zhangyancheng
 */
public class GetBit extends Request {

	public GetBit(String key, long offset) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(offset)
		);
	}
}
