package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class SubStr extends Request {

	public SubStr(String key, int start, int end) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(start),
				Util.toByteArray(end)
		);
	}
}
