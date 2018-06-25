package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class SetRange extends Request {

	public SetRange(String key, long offset, String value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(offset),
				ByteUtil.toByteArray(value)
		);
	}
}
