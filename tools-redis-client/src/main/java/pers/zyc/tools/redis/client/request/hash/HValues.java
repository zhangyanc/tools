package pers.zyc.tools.redis.client.request.hash;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.List;

/**
 * HVALS key
 * </p>
 *
 * 返回哈希表 key 中所有域的值。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N)， N 为哈希表的大小。</li>
 * <li>Redis版本要求: >=2.0.0</li>
 * <li>返回值: 一个包含哈希表中所有值的表。当 key 不存在时，返回一个空表。</li>
 * </ul>
 *
 *
 * @author zhangyancheng
 */
public class HValues extends AutoCastRequest<List<String>> {

	public HValues(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}

	@Override
	protected String getCommand() {
		return "HVALS";
	}
}
