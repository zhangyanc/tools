package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class Type extends Request {

	public Type(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}
}
