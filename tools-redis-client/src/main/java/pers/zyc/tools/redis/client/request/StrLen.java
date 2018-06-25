package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class StrLen extends Request {

	public StrLen(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}
}
