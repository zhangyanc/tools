package pers.zyc.tools.redis.client.request.key;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * RENAME key newkey
 * </p>
 *
 * 将 key 改名为 newkey 。
 * 当 key 和 newkey 相同，或者 key 不存在时，抛出异常。
 * 当 newkey 已经存在时， RENAME 命令将覆盖旧值。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 改名成功时无返回 ，失败时抛出异常。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Rename extends AutoCastRequest<Void> {

	public Rename(String key, String newKey) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(newKey)
		);
	}
}
