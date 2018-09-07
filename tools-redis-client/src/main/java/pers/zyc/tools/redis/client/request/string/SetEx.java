package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * SETEX key seconds value
 * </p>
 *
 * 将值 value 关联到 key ，并将 key 的生存时间设为 seconds (以秒为单位)。
 * 如果 key 已经存在， SETEX 命令将覆写旧值。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.0.0</li>
 * <li>返回值: 无, 除非异常</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class SetEx extends AutoCastRequest<Void> {

	public SetEx(String key, int seconds, String value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(seconds),
				ByteUtil.toByteArray(value)
		);
	}
}
