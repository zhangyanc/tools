package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class HSet extends Request {

	public HSet(String key, String field, String value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(field),
				ByteUtil.toByteArray(value)
		);
	}
}
