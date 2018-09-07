package pers.zyc.tools.redis.client.request.client;

import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * CLIENT REPLY ON|OFF|SKIP
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=3.2</li>
 * <li>返回值: 无</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class ClientReply extends Client<Void> {

	public ClientReply(String mode) {
		super(
				Mode.valueOf(mode.toUpperCase()).BS
		);
	}

	private enum Mode {
		@SuppressWarnings("unused") ON,
		@SuppressWarnings("unused") OFF,
		@SuppressWarnings("unused") SKIP;

		byte[] BS;

		Mode() {
			BS = ByteUtil.toByteArray(name());
		}
	}
}
