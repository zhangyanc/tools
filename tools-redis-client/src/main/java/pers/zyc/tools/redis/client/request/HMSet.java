package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.Map;

/**
 * @author zhangyancheng
 */
public class HMSet extends Request {

	public HMSet(String key, Map<String, String> hash) {
		parts.add(ByteUtil.toByteArray(key));
		for (Map.Entry<String, String> entry : hash.entrySet()) {
			parts.add(ByteUtil.toByteArray(entry.getKey()));
			parts.add(ByteUtil.toByteArray(entry.getValue()));
		}
	}
}
