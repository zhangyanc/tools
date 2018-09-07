package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * PSETEX key milliseconds value
 * </p>
 *
 * 这个命令和 SETEX 命令相似，但它以毫秒为单位设置 key 的生存时间，而不是像 SETEX 命令那样，以秒为单位。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.6.0</li>
 * <li>返回值: </li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class PSetEx extends Request<Void> {

	public PSetEx(String key, long milliseconds, String value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(milliseconds),
				ByteUtil.toByteArray(value)
		);
	}
}
