package pers.zyc.tools.redis.client;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pers.zyc.tools.redis.client.request.hash.HSet;
import pers.zyc.tools.redis.client.request.server.DBSize;
import pers.zyc.tools.redis.client.request.set.SRandomMember;
import pers.zyc.tools.utils.TimeMillis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author zhangyancheng
 */
public class ScanTest {

	private static final String TESTABLE_REDIS_SERVER = "localhost:6379";

	private ConnectionPool connectionPool;

	@Before
	public void setUp() throws Exception {
		ClientConfig config = new ClientConfig("redis://" + TESTABLE_REDIS_SERVER +
				"?minConnectionIdle=0&maxConnectionTotal=1&requestTimeout=1000000");
		connectionPool = new ConnectionPool(config);
	}

	@After
	public void tearDown() throws Exception {
		connectionPool.stop();
	}

	@Test
	public void case_SCAN() throws Exception {
		long keys = connectionPool.getConnection().send(new DBSize()).get();

		KeyScanner scanner = new KeyScanner(connectionPool);

		Set<String> result = new HashSet<>();
		while (!scanner.atEnd()) {
			result.addAll(scanner.scan().get());
		}

		Assert.assertEquals(keys, result.size());
	}

	@Test
	public void test_HSCAN() throws Exception {
		String key = "TestKey-random_" + TimeMillis.INSTANCE.get() + "" + Math.random();

		connectionPool.getConnection().send(new HSet(key, "k1", "v1")).get();
		connectionPool.getConnection().send(new HSet(key, "k2", "v2")).get();
		connectionPool.getConnection().send(new HSet(key, "k3", "v3")).get();


		HashScanner scanner = new HashScanner(key, connectionPool);

		Map<String, String> result = new HashMap<>();
		while (!scanner.atEnd()) {
			result.putAll(scanner.scan().get());
		}

		Assert.assertEquals("v1", result.remove("k1"));
		Assert.assertEquals("v2", result.remove("k2"));
		Assert.assertEquals("v3", result.remove("k3"));
		Assert.assertEquals(0, result.size());
	}

	@Test
	public void test_SSCAN() throws Exception {
		String key = "TestKey-random_" + TimeMillis.INSTANCE.get() + "" + Math.random();

		connectionPool.getConnection().send(new HSet(key, "k1", "v1")).get();
		connectionPool.getConnection().send(new HSet(key, "k2", "v2")).get();
		connectionPool.getConnection().send(new HSet(key, "k3", "v3")).get();
	}

	@Test
	public void case_SRandomMember() throws Exception {
		ClientConfig config = new ClientConfig("redis://" +TESTABLE_REDIS_SERVER +
				"?minConnectionIdle=1&maxConnectionTotal=1");
		//只有一个连接
		ConnectionPool connectionPool = new ConnectionPool(config);
		try {
			System.out.println(connectionPool.getConnection().send(new SRandomMember("xxx")).get());
		} finally {
			connectionPool.stop();
		}
	}
}
