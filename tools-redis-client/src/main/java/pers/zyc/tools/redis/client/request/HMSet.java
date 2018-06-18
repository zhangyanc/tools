package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

import java.util.Map;

/**
 * @author zhangyancheng
 */
public class HMSet extends Request {

	public HMSet(String key, Map<String, String> hash) {
		parts.add(Util.toByteArray(key));
		for (Map.Entry<String, String> entry : hash.entrySet()) {
			parts.add(Util.toByteArray(entry.getKey()));
			parts.add(Util.toByteArray(entry.getValue()));
		}
	}
}
