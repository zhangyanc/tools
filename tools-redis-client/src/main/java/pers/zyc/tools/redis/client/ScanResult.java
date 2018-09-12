package pers.zyc.tools.redis.client;

import pers.zyc.tools.utils.Pair;

import java.util.List;

/**
 * @author zhangyancheng
 */
public class ScanResult extends Pair<Long, List<String>> {

	public ScanResult(long cursor, List<String> keys) {
		key(cursor);
		value(keys);
	}

	public long getCursor() {
		return key();
	}

	public List<String> getList() {
		return value();
	}
}