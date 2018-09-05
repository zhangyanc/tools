package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class Auth extends Request<Void> {

	public Auth(String password) {
		super(
				ByteUtil.toByteArray(password)
		);
	}
}
