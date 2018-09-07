package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.ResponseCast;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * SET key value [EX seconds] [PX milliseconds] [NX|XX]
 * </p>
 *
 * 将字符串值 value 关联到 key 。
 * 如果 key 已经持有其他值， SET 就覆写旧值，无视类型。
 * 对于某个原本带有生存时间（TTL）的键来说， 当 SET 命令成功在这个键上执行时， 这个键原有的 TTL 将被清除。
 * </p>
 *
 * 从 Redis 2.6.12 版本开始， SET 命令的行为可以通过一系列参数来修改：
 * <ul>
 * <li>EX second ：设置键的过期时间为 second 秒。 SET key value EX second 效果等同于 SETEX key second value 。</li>
 * <li>PX millisecond ：设置键的过期时间为 millisecond 毫秒。
 * 	   SET key value PX millisecond 效果等同于 PSETEX key millisecond value 。</li>
 * <li>NX ：只在键不存在时，才对键进行设置操作。 SET key value NX 效果等同于 SETNX key value 。</li>
 * <li>XX ：只在键已经存在时，才对键进行设置操作。</li>
 * </ul>
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 设置操作成功完成时，返回 OK。
 *     		  如果设置了 NX 或者 XX ，但因为条件没达到而造成设置操作未执行，那么命令返回空批量回复（NULL Bulk Reply）。
 * </li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Set extends Request<Boolean> {

	public Set(String key, String value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(value)
		);
	}

	public Set(String key, String value, String nxxx, String expx, long time) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(value),
				NXXX.valueOf(nxxx.toUpperCase()).BS,
				EXPX.valueOf(expx.toUpperCase()).BS,
				ByteUtil.toByteArray(time)
		);
	}

	public Set(String key, String value, String nxxx) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(value),
				NXXX.valueOf(nxxx.toUpperCase()).BS
		);
	}

	private enum NXXX {
		@SuppressWarnings("unused") NX,
		@SuppressWarnings("unused") XX;

		final byte[] BS;

		NXXX() {
			this.BS = ByteUtil.toByteArray(name());
		}
	}

	private enum EXPX {
		@SuppressWarnings("unused") EX,
		@SuppressWarnings("unused") PX;

		final byte[] BS;

		EXPX() {
			this.BS = ByteUtil.toByteArray(name());
		}
	}

	@Override
	public ResponseCast<Boolean> getCast() {
		return SET_CAST;
	}

	private static final ResponseCast<Boolean> SET_CAST = new ResponseCast<Boolean>() {

		@Override
		public Boolean cast(Object response) {
			return response != null;
		}
	};
}
