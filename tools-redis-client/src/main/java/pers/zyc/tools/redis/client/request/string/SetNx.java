package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * SETNX key value
 * </p>
 *
 * 将 key 的值设为 value ，当且仅当 key 不存在。
 * 若给定的 key 已经存在，则 SETNX 不做任何动作。
 * SETNX 是『SET if Not eXists』(如果不存在，则 SET)的简写。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 设置成功，返回 true 。设置失败，返回 false 。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class SetNx extends AutoCastRequest<Boolean> {

	public SetNx(String key, String value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(value)
		);
	}
}
