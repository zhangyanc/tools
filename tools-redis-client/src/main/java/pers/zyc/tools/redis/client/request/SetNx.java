package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.Util;

/**
 * @author zhangyancheng
 */
public class SetNx extends Request {

	public SetNx(String key, String value) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(value)
		);
	}
}
