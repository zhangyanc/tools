package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class HDelete extends Request {

	public HDelete(String key, String... fields) {
		parts.add(ByteUtil.toByteArray(key));
		for (String field : fields) {
			parts.add(ByteUtil.toByteArray(field));
		}
	}

	@Override
	protected String getCommand() {
		return "HDEL";
	}
}
