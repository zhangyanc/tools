package pers.zyc.tools.redis.client.request.server;

import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * CLIENT SETNAME connection-name
 * </p>
 *
 * 为当前连接分配一个名字。
 * 这个名字会显示在 CLIENT LIST 命令的结果中， 用于识别当前正在与服务器进行连接的客户端。
 * 为了避免和 CLIENT LIST 命令的输出格式发生冲突， 名字里不允许使用空格。
 * 要移除一个连接的名字， 可以将连接的名字设为空字符串 "" 。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.6.9</li>
 * <li>返回值: 无，除非异常。</li>
 * </ul>
 *
 *
 * @author zhangyancheng
 */
public class ClientSetName extends Client<Void> {

	public ClientSetName(String clientName) {
		super(
				ByteUtil.toByteArray("SETNAME"),
				ByteUtil.toByteArray(clientName)
		);
	}
}
