package pers.zyc.tools.redis.client;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pers.zyc.tools.redis.client.request.hash.HSet;
import pers.zyc.tools.redis.client.request.server.DBSize;
import pers.zyc.tools.redis.client.request.set.SAdd;
import pers.zyc.tools.utils.SystemMillis;

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
		String key = "TestKey-random_" + SystemMillis.current() + "" + Math.random();

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
		String key1 = "TestKey-random_" + SystemMillis.current() + "" + Math.random();
		String key2 = "TestKey-random_" + SystemMillis.current() + "" + Math.random();

		connectionPool.getConnection().send(new SAdd(key1, "a", "b", "c")).get();
		connectionPool.getConnection().send(new SAdd(key2, "d")).get();

		SetScanner setScanner = new SetScanner(key1, connectionPool);
		Assert.assertEquals(3, setScanner.scan().get().size());

		setScanner = new SetScanner(key2, connectionPool);
		Assert.assertEquals(1, setScanner.scan().get().size());
	}

	@Test
	public void test_ZSCAN() throws Exception {

	}
}
