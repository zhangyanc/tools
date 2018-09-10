package pers.zyc.tools.redis.client.request.server;

import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * CLIENT PAUSE timeout
 * </p>
 *
 * CLIENT PAUSE是一个连接控制命令，用于挂起所有的客户端连接一段时间(毫秒)
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.9.50</li>
 * <li>返回值: 无，除非timeout参数异常。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class ClientPause extends Client<Void> {

	public ClientPause(int timeout) {
		super(
				ByteUtil.toByteArray(timeout)
		);
	}
}
