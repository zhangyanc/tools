package pers.zyc.tools.redis.client.request.hash;

import pers.zyc.tools.redis.client.request.BaseScan;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class HScan extends BaseScan {

	public HScan(String key, long cursor) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(cursor)
		);
	}

	public HScan(String key, long cursor, String match) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(cursor),
				ByteUtil.toByteArray("MATCH"),
				ByteUtil.toByteArray(match)
		);
	}

	public HScan(String key, long cursor, int count) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(cursor),
				ByteUtil.toByteArray("COUNT"),
				ByteUtil.toByteArray(count)
		);
	}

	public HScan(String key, long cursor, String match, int count) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(cursor),
				ByteUtil.toByteArray("MATCH"),
				ByteUtil.toByteArray(match),
				ByteUtil.toByteArray("COUNT"),
				ByteUtil.toByteArray(count)
		);
	}
}
