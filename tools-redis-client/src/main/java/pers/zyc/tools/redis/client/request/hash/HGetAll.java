package pers.zyc.tools.redis.client.request.hash;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.Map;

/**
 * HGETALL key
 * </p>
 *
 * 返回哈希表 key 中，所有的域和值组成的哈希表。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N)， N 为哈希表的大小。</li>
 * <li>Redis版本要求: >=2.0.0</li>
 * <li>返回值: 所有的域和值组成的哈希表。。若 key 不存在，返回空列表。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class HGetAll extends AutoCastRequest<Map<String, String>> {

	public HGetAll(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}
}
