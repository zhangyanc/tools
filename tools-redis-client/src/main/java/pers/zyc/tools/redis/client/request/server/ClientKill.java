package pers.zyc.tools.redis.client.request.server;

import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * CLIENT KILL ADDR ip:port/CLIENT KILL ID server-id
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N)，N 为连接到服务器的客户端数量。</li>
 * <li>Redis版本要求: >=2.8.12</li>
 * <li>返回值: 命令返回多行字符串。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class ClientKill extends Client<Long> {

	public ClientKill(String client) {
		super(
				ByteUtil.toByteArray("KILL"),
				ByteUtil.toByteArray(client)
		);
	}

	public ClientKill(String ip, int port) {
		super(
				ByteUtil.toByteArray("KILL"),
				ByteUtil.toByteArray("ADDR"),
				ByteUtil.toByteArray(ip + ":" + port)
		);
	}

	public ClientKill(int id) {
		super(
				ByteUtil.toByteArray("KILL"),
				ByteUtil.toByteArray("ID"),
				ByteUtil.toByteArray(id)
		);
	}
}
