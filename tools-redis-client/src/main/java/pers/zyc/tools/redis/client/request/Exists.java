package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class Exists extends Request {

	public Exists(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}
}
