package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * EXPIREAT key timestamp
 * </p>
 *
 * EXPIREAT 的作用和 {@link Expire} 类似，都用于为 key 设置生存时间。
 * 不同在于 EXPIREAT 命令接受的时间参数是 UNIX 时间戳(unix timestamp)。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.2.0</li>
 * <li>返回值: 设置成功返回 true ，否则返回 false 。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class ExpireAt extends Request<Boolean> {

	public ExpireAt(String key, long unixTime) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(unixTime)
		);
	}
}
