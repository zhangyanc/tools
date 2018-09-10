package pers.zyc.tools.redis.client.request.set;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.Set;

/**
 * SMEMBERS key
 * </p>
 *
 * 返回集合 key 中的所有成员。
 * 不存在的 key 被视为空集合。
 *
 * <ul>
 * <li>时间复杂度: O(N)， N 为集合的基数。</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 集合中的所有成员。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class SMembers extends AutoCastRequest<Set<String>> {

	public SMembers(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}
}
