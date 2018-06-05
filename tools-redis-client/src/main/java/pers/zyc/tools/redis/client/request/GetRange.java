package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class GetRange extends Request {

	public GetRange(String key, long startOffset, long endOffset) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(startOffset),
				Protocol.toByteArray(endOffset)
		);
	}
}
