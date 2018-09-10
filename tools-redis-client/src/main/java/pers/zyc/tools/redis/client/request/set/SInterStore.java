package pers.zyc.tools.redis.client.request.set;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * SINTERSTORE destination key [key ...]
 * </p>
 *
 * 这个命令类似于 SINTER 命令，但它将结果保存到 destination 集合，而不是简单地返回结果集。
 * 如果 destination 集合已经存在，则将其覆盖。
 * destination 可以是 key 本身。
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
public class SInterStore extends AutoCastRequest<Long> {

	public SInterStore(String destination, String key, String... keys) {
		bulks.add(ByteUtil.toByteArray(destination));
		bulks.add(ByteUtil.toByteArray(key));
		for (String k : keys) {
			bulks.add(ByteUtil.toByteArray(k));
		}
	}
}
