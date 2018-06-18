package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class HDelete extends Request {

	public HDelete(String key, String... fields) {
		parts.add(Util.toByteArray(key));
		for (String field : fields) {
			parts.add(Util.toByteArray(field));
		}
	}

	@Override
	protected String getCommand() {
		return "HDEL";
	}
}
