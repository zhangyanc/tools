package pers.zyc.tools.redis.client.request.key;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * PERSIST key
 * </p>
 *
 * 移除给定 key 的生存时间，将这个 key 从『易失的』(带生存时间 key )转换成『持久的』
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.2.0</li>
 * <li>返回值: 当生存时间移除成功时，返回 true。如果 key 不存在或 key 没有设置生存时间，返回 false 。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Persist extends Request<Boolean> {

	public Persist(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}
}
