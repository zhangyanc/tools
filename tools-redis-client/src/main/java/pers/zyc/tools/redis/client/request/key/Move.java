package pers.zyc.tools.redis.client.request.key;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * MOVE key db
 * </p>
 *
 * 将当前数据库的 key 移动到给定的数据库 db 当中。
 * 如果当前数据库(源数据库)和给定数据库(目标数据库)有相同名字的给定 key ，或者 key 不存在于当前数据库，那么 MOVE 没有任何效果。
 * 因此，也可以利用这一特性，将 MOVE 当作锁(locking)原语(primitive)。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 移动成功返回 true ，失败则返回 false 。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Move extends AutoCastRequest<Boolean> {

	public Move(String key, int db) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(db)
		);
	}
}
