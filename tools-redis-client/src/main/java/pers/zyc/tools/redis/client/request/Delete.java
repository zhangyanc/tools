package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class Delete extends Request {

	public Delete(String key) {
		super(
				Util.toByteArray(key)
		);
	}

	@Override
	protected String getCommand() {
		return "DEL";
	}
}
