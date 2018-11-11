package pers.zyc.tools.redis.client.request.set;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.Set;

/**
 * SUNION key [key ...]
 * </p>
 *
 * 返回一个集合的全部成员，该集合是所有给定集合的并集。
 * 不存在的 key 被视为空集。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N)， N 是所有给定集合的成员数量之和。</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 并集成员的集合。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class SUnion extends AutoCastRequest<Set<String>> {

	public SUnion(String key, String... keys) {
		super(ByteUtil.toByteArray(key));
		for (String k : keys) {
			bulks.add(ByteUtil.toByteArray(k));
		}
	}
}
