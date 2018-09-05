package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * TTL key
 * </p>
 *
 * 以秒为单位，返回给定 key 的剩余生存时间(TTL, time to live)。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 当 key 不存在时，返回 -2 。
 * 			  当 key 存在但没有设置剩余生存时间时，返回 -1 。
 * 			  否则，以秒为单位，返回 key 的剩余生存时间。
 * </li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Ttl extends Request<Long> {

	public Ttl(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}
}
