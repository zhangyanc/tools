package pers.zyc.tools.redis.client.request.key;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * PEXPIRE key milliseconds
 * </p>
 *
 * 这个命令和 EXPIRE 命令的作用类似，但是它以毫秒为单位设置 key 的生存时间，而不像 EXPIRE 命令那样，以秒为单位。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.6.0</li>
 * <li>返回值: 设置成功，返回 true。如果 key 不存在或设置失败，返回 false 。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class PExpire extends Request<Boolean> {

	public PExpire(String key, long milliseconds) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(milliseconds)
		);
	}
}
