package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class HMGet extends Request {

	public HMGet(String key, String... fields) {
		bulks.add(ByteUtil.toByteArray(key));
		for (String field : fields) {
			bulks.add(ByteUtil.toByteArray(field));
		}
	}
}
