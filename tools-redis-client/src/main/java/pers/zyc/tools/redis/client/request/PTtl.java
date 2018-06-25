package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.Util;

/**
 * @author zhangyancheng
 */
public class PTtl extends Request {

	public PTtl(String key) {
		super(
				Util.toByteArray(key)
		);
	}
}
