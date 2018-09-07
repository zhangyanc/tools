package pers.zyc.tools.redis.client.request.key;

import pers.zyc.tools.redis.client.request.BaseScan;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * @author zhangyancheng
 */
public class Scan extends BaseScan {

	public Scan(long cursor) {
		super(
				ByteUtil.toByteArray(cursor)
		);
	}

	public Scan(long cursor, String match) {
		super(
				ByteUtil.toByteArray(cursor),
				ByteUtil.toByteArray("MATCH"),
				ByteUtil.toByteArray(match)
		);
	}

	public Scan(long cursor, int count) {
		super(
				ByteUtil.toByteArray(cursor),
				ByteUtil.toByteArray("COUNT"),
				ByteUtil.toByteArray(count)
		);
	}

	public Scan(long cursor, String match, int count) {
		super(
				ByteUtil.toByteArray(cursor),
				ByteUtil.toByteArray("MATCH"),
				ByteUtil.toByteArray(match),
				ByteUtil.toByteArray("COUNT"),
				ByteUtil.toByteArray(count)
		);
	}
}
