package pers.zyc.tools.redis.client.request.key;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * RENAMENX key newkey
 * </p>
 *
 * 当且仅当 newkey 不存在时，将 key 改名为 newkey 。
 * 当 key 不存在时，抛出异常。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 修改成功时，返回 true 。如果 newkey 已经存在，返回 false 。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class RenameNx extends AutoCastRequest<Boolean> {

	public RenameNx(String key, String newKey) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(newKey)
		);
	}
}
