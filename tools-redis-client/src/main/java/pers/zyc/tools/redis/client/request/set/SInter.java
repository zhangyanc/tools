package pers.zyc.tools.redis.client.request.set;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.Set;

/**
 * SINTER key [key ...]
 * </p>
 *
 * 返回一个集合的全部成员，该集合是所有给定集合的交集。
 * 不存在的 key 被视为空集。
 * 当给定集合当中有一个空集时，结果也为空集(根据集合运算定律)。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N * M)， N 为给定集合当中基数最小的集合， M 为给定集合的个数。</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 交集成员的列表。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class SInter extends AutoCastRequest<Set<String>> {

	public SInter(String key, String... keys) {
		bulks.add(ByteUtil.toByteArray(key));
		for (String k : keys) {
			bulks.add(ByteUtil.toByteArray(k));
		}
	}
}
