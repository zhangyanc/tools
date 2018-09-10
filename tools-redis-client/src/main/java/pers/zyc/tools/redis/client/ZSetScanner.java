package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.request.sortedset.ZScan;
import pers.zyc.tools.redis.client.util.Future;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author zhangyancheng
 */
public class ZSetScanner extends BaseScanner<Map<String, Double>> {

	private final String key;

	public ZSetScanner(String key, ConnectionPool connectionPool) {
		super(connectionPool);
		this.key = key;
	}

	@Override
	public Future<Map<String, Double>> scan() {
		return scan(new ZScan(key, cursor));
	}

	@Override
	public Future<Map<String, Double>> scan(String match) {
		return scan(new ZScan(key, cursor, match));
	}

	@Override
	public Future<Map<String, Double>> scan(int count) {
		return scan(new ZScan(key, cursor, count));
	}

	@Override
	public Future<Map<String, Double>> scan(String match, int count) {
		return scan(new ZScan(key, cursor, match, count));
	}

	@Override
	protected Map<String, Double> parseList(List<String> stringList) {
		Iterator<String> iterator = stringList.iterator();

		Map<String, Double> result = new HashMap<>();
		while (iterator.hasNext()) {
			result.put(iterator.next(), Double.parseDouble(iterator.next()));
		}
		return result;
	}
}
