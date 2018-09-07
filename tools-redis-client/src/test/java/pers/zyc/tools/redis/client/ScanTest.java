package pers.zyc.tools.redis.client;

import org.junit.Test;

import java.util.*;

/**
 * @author zhangyancheng
 */
public class ScanTest {

	private static final String TESTABLE_REDIS_SERVER = "localhost:6379";

	@Test
	public void test_scan() throws Exception {
		ClientConfig config = new ClientConfig("redis://" + TESTABLE_REDIS_SERVER +
				"?minConnectionIdle=0&maxConnectionTotal=1&requestTimeout=1000000");
		ConnectionPool connectionPool = new ConnectionPool(config);

		try {
			HashScanner scanner = new HashScanner("map1", connectionPool);

			Map<String, String> result = new HashMap<>();
			while (!scanner.atEnd()) {
				result.putAll(scanner.scan().get());
			}

			System.out.println(result);
		} finally {
			connectionPool.stop();
		}
	}
}
