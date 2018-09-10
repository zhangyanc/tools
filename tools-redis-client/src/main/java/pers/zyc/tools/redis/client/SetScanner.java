package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.request.set.SScan;
import pers.zyc.tools.redis.client.util.Future;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zhangyancheng
 */
public class SetScanner extends BaseScanner<Set<String>> {

	private final String key;

	public SetScanner(String key, ConnectionPool connectionPool) {
		super(connectionPool);
		this.key = key;
	}

	@Override
	public Future<Set<String>> scan() {
		return scan(new SScan(key, cursor));
	}

	@Override
	public Future<Set<String>> scan(String match) {
		return scan(new SScan(key, cursor, match));
	}

	@Override
	public Future<Set<String>> scan(int count) {
		return scan(new SScan(key, cursor, count));
	}

	@Override
	public Future<Set<String>> scan(String match, int count) {
		return scan(new SScan(key, cursor, match, count));
	}

	@Override
	protected Set<String> parseList(List<String> stringList) {
		return new HashSet<>(stringList);
	}
}
