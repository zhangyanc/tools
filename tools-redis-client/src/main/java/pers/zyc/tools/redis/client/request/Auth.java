package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class Auth extends Request {

	public Auth(String password) {
		super(
				Util.toByteArray(password)
		);
	}
}
