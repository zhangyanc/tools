package pers.zyc.tools.redis.client.request.set;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * SDIFFSTORE destination key [key ...]
 * </p>
 *
 * 这个命令的作用和 SDIFF 类似，但它将结果保存到 destination 集合，而不是简单地返回结果集。
 * 如果 destination 集合已经存在，则将其覆盖。
 * destination 可以是 key 本身。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N)，N 是所有给定集合的成员数量之和。</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 结果集中的元素数量。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class SDiffStore extends AutoCastRequest<Long> {

	public SDiffStore(String destination, String key, String... keys) {
		bulks.add(ByteUtil.toByteArray(destination));
		bulks.add(ByteUtil.toByteArray(key));
		for (String k : keys) {
			bulks.add(ByteUtil.toByteArray(k));
		}
	}
}
