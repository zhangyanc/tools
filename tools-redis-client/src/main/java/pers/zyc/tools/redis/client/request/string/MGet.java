package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.List;

/**
 * MGET key [key ...]
 * </p>
 *
 * 返回所有(一个或多个)给定 key 的值。
 * 如果给定的 key 里面，有某个 key 不存在，那么这个 key 返回 null 。因此，该命令永不失败。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N) , N 为给定 key 的数量。</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 一个包含所有给定 key 的值的列表。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class MGet extends AutoCastRequest<List<String>> {

	public MGet(String... keys) {
		for (String key : keys) {
			bulks.add(ByteUtil.toByteArray(key));
		}
	}
}
