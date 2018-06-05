package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class Set extends Request {

	public Set(String key, String value) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(value)
		);
	}

	public Set(String key, String value, String nxxx, String expx, long time) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(value),
				NXXX.valueOf(nxxx.toUpperCase()).BS,
				EXPX.valueOf(expx.toUpperCase()).BS,
				Protocol.toByteArray(time)
		);
	}

	public Set(String key, String value, String nxxx) {
		super(
				Protocol.toByteArray(key),
				Protocol.toByteArray(value),
				NXXX.valueOf(nxxx.toUpperCase()).BS
		);
	}

	private enum NXXX {
		@SuppressWarnings("unused") NX,
		@SuppressWarnings("unused") XX;

		final byte[] BS;

		NXXX() {
			this.BS = Protocol.toByteArray(name());
		}
	}

	private enum EXPX {
		@SuppressWarnings("unused") EX,
		@SuppressWarnings("unused") PX;

		final byte[] BS;

		EXPX() {
			this.BS = Protocol.toByteArray(name());
		}
	}
}
