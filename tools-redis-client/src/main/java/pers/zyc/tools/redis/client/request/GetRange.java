package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class GetRange extends Request {

	public GetRange(String key, long startOffset, long endOffset) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(startOffset),
				ByteUtil.toByteArray(endOffset)
		);
	}
}
