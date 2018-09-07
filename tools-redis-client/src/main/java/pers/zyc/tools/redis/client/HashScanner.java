package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.request.hash.HScan;
import pers.zyc.tools.redis.client.util.Future;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author zhangyancheng
 */
public class HashScanner extends BaseScanner<Map<String, String>> {

	private final String key;

	public HashScanner(String key, ConnectionPool connectionPool) {
		super(connectionPool);
		this.key = key;
	}

	@Override
	public Future<Map<String, String>> scan() {
		return scan(new HScan(key, cursor));
	}

	@Override
	public Future<Map<String, String>> scan(String match) {
		return scan(new HScan(key, cursor, match));
	}

	@Override
	public Future<Map<String, String>> scan(int count) {
		return scan(new HScan(key, cursor, count));
	}

	@Override
	public Future<Map<String, String>> scan(String match, int count) {
		return scan(new HScan(key, cursor, match, count));
	}

	@Override
	protected Map<String, String> parseList(List<String> stringList) {
		Iterator<String> iterator = stringList.iterator();

		Map<String, String> result = new HashMap<>();
		while (iterator.hasNext()) {
			result.put(iterator.next(), iterator.next());
		}
		return result;
	}
}
