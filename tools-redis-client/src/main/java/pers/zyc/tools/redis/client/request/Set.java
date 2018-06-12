package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class Set extends Request {

	public Set(String key, String value) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(value)
		);
	}

	public Set(String key, String value, String nxxx, String expx, long time) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(value),
				NXXX.valueOf(nxxx.toUpperCase()).BS,
				EXPX.valueOf(expx.toUpperCase()).BS,
				Util.toByteArray(time)
		);
	}

	public Set(String key, String value, String nxxx) {
		super(
				Util.toByteArray(key),
				Util.toByteArray(value),
				NXXX.valueOf(nxxx.toUpperCase()).BS
		);
	}

	private enum NXXX {
		@SuppressWarnings("unused") NX,
		@SuppressWarnings("unused") XX;

		final byte[] BS;

		NXXX() {
			this.BS = Util.toByteArray(name());
		}
	}

	private enum EXPX {
		@SuppressWarnings("unused") EX,
		@SuppressWarnings("unused") PX;

		final byte[] BS;

		EXPX() {
			this.BS = Util.toByteArray(name());
		}
	}
}
