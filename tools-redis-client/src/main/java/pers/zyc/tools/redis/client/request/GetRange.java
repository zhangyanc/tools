package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.Util;

/**
 * @author zhangyancheng
 */
public class GetRange extends Request {

	public GetRange(String key, long startOffset, long endOffset) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(startOffset),
				Util.toByteArray(endOffset)
		);
	}
}
