package pers.zyc.tools.redis.client.request.client;

import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * CLIENT GETNAME
 * </p>
 *
 * 返回 CLIENT SETNAME 命令为连接设置的名字。
 * 因为新创建的连接默认是没有名字的， 对于没有名字的连接， CLIENT GETNAME 返回空白回复。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.6.9</li>
 * <li>返回值: 连接设置的名字或者null。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class ClientGetName extends Client<String> {

	public ClientGetName() {
		super(
				ByteUtil.toByteArray("GETNAME")
		);
	}
}
