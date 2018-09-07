package pers.zyc.tools.redis.client.request.connection;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * AUTH password
 * </p>
 *
 * 通过设置配置文件中 requirepass 项的值(使用命令 CONFIG SET requirepass password )，可以使用密码来保护 Redis 服务器。
 * 如果开启了密码保护的话，在每次连接 Redis 服务器之后，就要使用 AUTH 命令解锁，解锁之后才能使用其他 Redis 命令。
 * 如果 AUTH 命令给定的密码 password 和配置文件中的密码相符的话，服务器会返回 OK 并开始接受命令输入。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 密码匹配时返回 OK(void) ，否则抛出异常。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Auth extends Request<Void> {

	public Auth(String password) {
		super(
				ByteUtil.toByteArray(password)
		);
	}
}
