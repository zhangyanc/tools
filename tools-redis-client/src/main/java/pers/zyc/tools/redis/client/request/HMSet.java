package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.Map;

/**
 * @author zhangyancheng
 */
public class HMSet extends Request {

	public HMSet(String key, Map<String, String> hash) {
		bulks.add(ByteUtil.toByteArray(key));
		for (Map.Entry<String, String> entry : hash.entrySet()) {
			bulks.add(ByteUtil.toByteArray(entry.getKey()));
			bulks.add(ByteUtil.toByteArray(entry.getValue()));
		}
	}
}
