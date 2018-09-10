package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.request.key.Scan;
import pers.zyc.tools.redis.client.util.Future;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zhangyancheng
 */
public class KeyScanner extends BaseScanner<Set<String>> {

	public KeyScanner(ConnectionPool connectionPool) {
		super(connectionPool);
	}

	@Override
	public Future<Set<String>> scan() {
		return scan(new Scan(cursor));
	}

	@Override
	public Future<Set<String>> scan(String match) {
		return scan(new Scan(cursor, match));
	}

	@Override
	public Future<Set<String>> scan(int count) {
		return scan(new Scan(cursor, count));
	}

	@Override
	public Future<Set<String>> scan(String match, int count) {
		return scan(new Scan(cursor, match, count));
	}

	@Override
	protected Set<String> parseList(List<String> stringList) {
		return new HashSet<>(stringList);
	}
}
