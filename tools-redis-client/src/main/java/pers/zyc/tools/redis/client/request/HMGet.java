package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.List;

/**
 * HMGET key field [field ...]
 * </p>
 *
 * 返回哈希表 key 中，一个或多个给定域的值。
 * 如果给定的域不存在于哈希表，那么返回一个 null 值。
 * 因为不存在的 key 被当作一个空哈希表来处理，所以对一个不存在的 key 进行 HMGET 操作将返回一个只带有 null 值的列表。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N)， N 为给定域的数量。</li>
 * <li>Redis版本要求: >=2.0.0</li>
 * <li>返回值: 一个包含多个给定域的关联值的表，表值的排列顺序和给定域参数的请求顺序一样。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class HMGet extends Request<List<String>> {

	public HMGet(String key, String... fields) {
		bulks.add(ByteUtil.toByteArray(key));
		for (String field : fields) {
			bulks.add(ByteUtil.toByteArray(field));
		}
	}
}
