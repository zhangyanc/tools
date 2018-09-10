package pers.zyc.tools.redis.client.request.set;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.List;

/**
 * SRANDMEMBER key [count]
 * </p>
 *
 * 从 Redis 2.6 版本开始， SRANDMEMBER 命令接受可选的 count 参数：
 * <ul>
 *     <li>
 *         如果 count 为正数，且小于集合基数，那么命令返回一个包含 count 个元素的数组，数组中的元素各不相同。如果 count 大于等于集
 *         合基数，那么返回整个集合。
 *     </li>
 *     <li>
 *         如果 count 为负数，那么命令返回一个数组，数组中的元素可能会重复出现多次，而数组的长度为 count 的绝对值。
 *     </li>
 * </ul>
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N) ，N 为返回数组的元素个数。</li>
 * <li>Redis版本要求: >=2.6.0</li>
 * <li>返回值: 随机元素组成的数组；如果集合为空，返回空数组。</li>
 * </ul>
 *
 * @see SRandomMember
 * @author zhangyancheng
 */
public class SRandomMemberWithCount extends AutoCastRequest<List<String>> {

	public SRandomMemberWithCount(String key, int count) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(count)
		);
	}

	@Override
	protected String getCommand() {
		return "SRANDMEMBER";
	}
}
