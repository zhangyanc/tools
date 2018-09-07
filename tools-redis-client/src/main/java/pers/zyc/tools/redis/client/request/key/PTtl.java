package pers.zyc.tools.redis.client.request.key;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * PTTL key
 * </p>
 *
 * 这个命令类似于 TTL 命令，但它以毫秒为单位返回 key 的剩余生存时间，而不是像 TTL 命令那样，以秒为单位。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.6.0</li>
 * <li>返回值: 当 key 不存在时，返回 -2 。
 * 			  当 key 存在但没有设置剩余生存时间时，返回 -1 。
 * 			  否则，以毫秒为单位，返回 key 的剩余生存时间。
 * 			  在 Redis 2.8 以前，当 key 不存在，或者 key 没有设置剩余生存时间时，命令都返回 -1 。
 * </li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class PTtl extends AutoCastRequest<Long> {

	public PTtl(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}
}
