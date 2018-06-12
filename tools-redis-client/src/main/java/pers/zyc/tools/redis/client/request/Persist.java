package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class Persist extends Request {

	public Persist(String key) {
		super(
				Util.toByteArray(key)
		);
	}
}
