package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class HMGet extends Request {

	public HMGet(String key, String... fields) {
		parts.add(Util.toByteArray(key));
		for (String field : fields) {
			parts.add(Util.toByteArray(field));
		}
	}
}
