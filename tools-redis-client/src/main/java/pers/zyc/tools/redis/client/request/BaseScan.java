package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.ResponseCast;
import pers.zyc.tools.redis.client.ScanResult;
import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangyancheng
 */
public abstract class BaseScan extends Request<ScanResult> {

	protected BaseScan(byte[]... bulks) {
		super(bulks);
	}

	@Override
	public ResponseCast<ScanResult> getCast() {
		return SCAN_CAST;
	}

	private static final ResponseCast<ScanResult> SCAN_CAST = new ResponseCast<ScanResult>() {

		@SuppressWarnings("unchecked")
		@Override
		public ScanResult cast(Object response) {
			List<Object> scanRet = (List<Object>) response;

			long cursor = Long.parseLong(ByteUtil.bytesToString((byte[]) scanRet.get(0)));
			List<String> keys = new ArrayList<>();

			for (byte[] key : (List<byte[]>) scanRet.get(1)) {
				keys.add(ByteUtil.bytesToString(key));
			}
			return new ScanResult(cursor, keys);
		}
	};
}
