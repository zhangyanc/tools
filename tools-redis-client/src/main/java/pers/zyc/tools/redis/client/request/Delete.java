package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * DEL key [key ...]
 * </p>
 *
 * 删除给定的一个或多个 key 。
 * 不存在的 key 会被忽略。
 *
 * <ul>
 * <li>时间复杂度:
 * 		O(N)， N 为被删除的 key 的数量。
 * 		删除单个字符串类型的 key ，时间复杂度为O(1)。
 * 		删除单个列表、集合、有序集合或哈希表类型的 key ，时间复杂度为O(M)， M 为以上数据结构内的元素数量。
 * </li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 被删除 key 的数量。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Delete extends Request<Long> {

	public Delete(String... keys) {
		for (String key :keys) {
			bulks.add(ByteUtil.toByteArray(key));
		}
	}

	@Override
	protected String getCommand() {
		return "DEL";
	}
}
