package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * SUBSTR key start end
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N)</li>
 * <li>Redis版本要求: <=2.0.0</li>
 * <li>返回值: 截取得出的子字符串。</li>
 * </ul>
 *
 * @see GetRange
 *
 * @author zhangyancheng
 */
public class SubStr extends Request<String> {

	public SubStr(String key, int start, int end) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(start),
				ByteUtil.toByteArray(end)
		);
	}
}
