package pers.zyc.tools.redis.client.request.set;

import pers.zyc.tools.redis.client.request.BaseScan;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * SSCAN key cursor [MATCH pattern] [COUNT count]
 *
 * @see pers.zyc.tools.redis.client.request.key.Scan
 * @author zhangyancheng
 */
public class SScan extends BaseScan {

	public SScan(String key, long cursor) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(cursor)
		);
	}

	public SScan(String key, long cursor, String match) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(cursor),
				ByteUtil.toByteArray("MATCH"),
				ByteUtil.toByteArray(match)
		);
	}

	public SScan(String key, long cursor, int count) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(cursor),
				ByteUtil.toByteArray("COUNT"),
				ByteUtil.toByteArray(count)
		);
	}

	public SScan(String key, long cursor, String match, int count) {
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
