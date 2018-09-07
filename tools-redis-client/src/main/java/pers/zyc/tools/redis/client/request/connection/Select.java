package pers.zyc.tools.redis.client.request.connection;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * SELECT index
 * </p>
 *
 * 切换到指定的数据库，数据库索引号 index 用数字值指定，以 0 作为起始索引值。
 * 默认使用 0 号数据库。
 *
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 无, 除非异常</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Select extends AutoCastRequest<Void> {

	public Select(int index) {
		super(
				ByteUtil.toByteArray(index)
		);
	}
}
