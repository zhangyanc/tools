package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class HGet extends Request {

	public HGet(String key, String field) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(field)
		);
	}
}
