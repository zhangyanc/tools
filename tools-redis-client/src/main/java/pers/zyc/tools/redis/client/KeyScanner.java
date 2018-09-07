package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.request.key.Scan;
import pers.zyc.tools.redis.client.util.Future;

import java.util.List;

/**
 * @author zhangyancheng
 */
public class KeyScanner extends BaseScanner<List<String>> {

	public KeyScanner(ConnectionPool connectionPool) {
		super(connectionPool);
	}

	@Override
	public Future<List<String>> scan() {
		return scan(new Scan(cursor));
	}

	@Override
	public Future<List<String>> scan(String match) {
		return scan(new Scan(cursor, match));
	}

	@Override
	public Future<List<String>> scan(int count) {
		return scan(new Scan(cursor, count));
	}

	@Override
	public Future<List<String>> scan(String match, int count) {
		return scan(new Scan(cursor, match, count));
	}

	@Override
	protected List<String> parseList(List<String> stringList) {
		return stringList;
	}
}
