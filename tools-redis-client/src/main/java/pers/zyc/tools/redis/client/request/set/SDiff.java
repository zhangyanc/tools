package pers.zyc.tools.redis.client.request.set;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.Set;

/**
 * SDIFF key [key ...]
 * </p>
 *
 * 返回一个集合的全部成员，该集合是所有给定集合之间的差集。
 * 不存在的 key 被视为空集。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N)，N 是所有给定集合的成员数量之和。</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 差集成员的列表。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class SDiff extends AutoCastRequest<Set<String>> {

	public SDiff(String key, String... keys) {
		bulks.add(ByteUtil.toByteArray(key));
		for (String k : keys) {
			bulks.add(ByteUtil.toByteArray(k));
		}
	}
}
