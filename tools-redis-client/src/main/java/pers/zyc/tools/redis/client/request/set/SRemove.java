package pers.zyc.tools.redis.client.request.set;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * SREM key member [member ...]
 * </p>
 *
 * 移除集合 key 中的一个或多个 member 元素，不存在的 member 元素会被忽略。
 *
 * 当 key 不是集合类型，将抛出异常。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N)， N 为给定 member 元素的数量。</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 被成功移除的元素的数量，不包括被忽略的元素。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class SRemove extends AutoCastRequest<Long> {

	public SRemove(String key, String member, String... members) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(member)
		);
		for (String m : members) {
			bulks.add(ByteUtil.toByteArray(m));
		}
	}

	@Override
	protected String getCommand() {
		return "SREM";
	}
}
