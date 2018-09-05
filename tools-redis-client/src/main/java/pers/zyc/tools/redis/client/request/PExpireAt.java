package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * PEXPIREAT key milliseconds-timestamp
 * </p>
 *
 * 这个命令和 EXPIREAT 命令类似，但它以毫秒为单位设置 key 的过期 unix 时间戳，而不是像 EXPIREAT 那样，以秒为单位。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.6.0</li>
 * <li>返回值: 设置成功，返回 true。如果 key 不存在或设置失败，返回 false 。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class PExpireAt extends Request<Boolean> {

	public PExpireAt(String key, long millisecondsTimestamp) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(millisecondsTimestamp)
		);
	}
}
