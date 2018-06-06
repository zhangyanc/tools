package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;
import pers.zyc.tools.redis.client.Request;

/**
 * @author zhangyancheng
 */
public class SubStr extends Request {

	public SubStr(String key, int start, int end) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(start),
				Protocol.toByteArray(end)
		);
	}
}
