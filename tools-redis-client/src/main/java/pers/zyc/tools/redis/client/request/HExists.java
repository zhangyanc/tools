package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class HExists extends Request {

	public HExists(String key, String field) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(field)
		);
	}
}
