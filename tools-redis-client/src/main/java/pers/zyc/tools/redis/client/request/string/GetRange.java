package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * GETRANGE key start end
 * </p>
 * 返回 key 中字符串值的子字符串，字符串的截取范围由 start 和 end 两个偏移量决定(包括 start 和 end 在内)。
 * 负数偏移量表示从字符串最后开始计数， -1 表示最后一个字符， -2 表示倒数第二个，以此类推。(不支持回绕操作)
 * GETRANGE 通过保证子字符串的值域(range)不超过实际字符串的值域来处理超出范围的值域请求。
 *
 * 在 <= 2.0 的版本里，GETRANGE 被叫作 SUBSTR。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N)</li>
 * <li>Redis版本要求: >=2.4.0</li>
 * <li>返回值: 截取得出的子字符串。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class GetRange extends Request<String> {

	public GetRange(String key, long startOffset, long endOffset) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(startOffset),
				ByteUtil.toByteArray(endOffset)
		);
	}
}
