package pers.zyc.tools.redis.client.request.key;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * EXPIRE key seconds
 * </p>
 *
 * 对一个已经带有生存时间的 key 执行 EXPIRE 命令，新指定的生存时间会取代旧的生存时间。
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 设置成功返回 true ，否则返回 false 。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Expire extends Request<Boolean> {

	public Expire(String key, int seconds) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(seconds)
		);
	}
}
