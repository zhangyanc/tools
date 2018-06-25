package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class GetBit extends Request {

	public GetBit(String key, long offset) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(offset)
		);
	}
}
