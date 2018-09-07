package pers.zyc.tools.redis.client.request.connection;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * ECHO message
 * </p>
 *
 * 打印一个特定的信息 message ，测试时使用。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: message 自身。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Echo extends AutoCastRequest<String> {

	public Echo(String message) {
		super(
				ByteUtil.toByteArray(message)
		);
	}
}
