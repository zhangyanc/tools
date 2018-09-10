package pers.zyc.tools.redis.client.request.set;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * SPOP key
 * </p>
 *
 * 移除并返回集合中的一个随机元素。
 * 如果只想获取一个随机元素，但不想该元素从集合中被移除的话，可以使用 SRANDMEMBER 命令。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 被移除的随机元素。当 key 不存在或 key 是空集时，返回 null 。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class SPop extends AutoCastRequest<String> {

	public SPop(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}
}
