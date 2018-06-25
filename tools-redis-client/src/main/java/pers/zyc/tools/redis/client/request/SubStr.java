package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class SubStr extends Request {

	public SubStr(String key, int start, int end) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(start),
				ByteUtil.toByteArray(end)
		);
	}
}
