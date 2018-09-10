package pers.zyc.tools.redis.client.request.sortedset;

import pers.zyc.tools.redis.client.request.BaseScan;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * ZSCAN key cursor [MATCH pattern] [COUNT count]
 *
 * @see pers.zyc.tools.redis.client.request.key.Scan
 * @author zhangyancheng
 */
public class ZScan extends BaseScan {

	public ZScan(String key, long cursor) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(cursor)
		);
	}

	public ZScan(String key, long cursor, String match) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(cursor),
				ByteUtil.toByteArray("MATCH"),
				ByteUtil.toByteArray(match)
		);
	}

	public ZScan(String key, long cursor, int count) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(cursor),
				ByteUtil.toByteArray("COUNT"),
				ByteUtil.toByteArray(count)
		);
	}

	public ZScan(String key, long cursor, String match, int count) {
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
