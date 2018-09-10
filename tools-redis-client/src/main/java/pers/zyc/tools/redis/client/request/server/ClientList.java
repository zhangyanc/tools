package pers.zyc.tools.redis.client.request.server;

import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * CLIENT LIST
 * </p>
 *
 * 以人类可读的格式，返回所有连接到服务器的客户端信息和统计数据。
 * </p>
 *
 * addr=127.0.0.1:43143 fd=6 age=183 idle=0 flags=N db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=32768 obl=0 oll=0 omem=0 events=r cmd=server
 * addr=127.0.0.1:43163 fd=5 age=35 idle=15 flags=N db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=0 obl=0 oll=0 omem=0 events=r cmd=ping
 * addr=127.0.0.1:43167 fd=7 age=24 idle=6 flags=N db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=0 obl=0 oll=0 omem=0 events=r cmd=get
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N)，N 为连接到服务器的客户端数量。</li>
 * <li>Redis版本要求: >=2.4.0</li>
 * <li>返回值: 命令返回多行字符串。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class ClientList extends Client<String> {

	public ClientList() {
		super(
				ByteUtil.toByteArray("LIST")
		);
	}
}
