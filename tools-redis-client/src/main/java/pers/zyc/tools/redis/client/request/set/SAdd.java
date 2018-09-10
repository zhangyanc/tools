package pers.zyc.tools.redis.client.request.set;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * SADD key member [member ...]
 * </p>
 *
 * 将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略。
 * 假如 key 不存在，则创建一个只包含 member 元素作成员的集合。
 * 当 key 不是集合类型时，抛出异常。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N)， N 是被添加的元素的数量。</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 被添加到集合中的新元素的数量，不包括被忽略的元素。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class SAdd extends AutoCastRequest<Long> {

	public SAdd(String key, String... members) {
		bulks.add(ByteUtil.toByteArray(key));
		for (String member : members) {
			bulks.add(ByteUtil.toByteArray(member));
		}
	}
}
