package pers.zyc.tools.redis.client;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pers.zyc.tools.utils.TimeMillis;

/**
 * Nonstandard unit tests.
 *
 * @author zhangyancheng
 */
public class RedisClientTest {

	private static final String TESTABLE_REDIS_SERVER = "localhost:6379";

	private RedisClient redisClient;

	@Before
	public void setUp() throws Exception {
		ClientConfig config = new ClientConfig("redis://" + TESTABLE_REDIS_SERVER);

		redisClient = new RedisClient(config);
	}

	@After
	public void tearDown() {
		redisClient.close();
	}

	@Test
	public void case_SET_RandomKey_OKStatus() {
		String key = "TestKey-random_" + TimeMillis.INSTANCE.get() + "" + Math.random();
		redisClient.set(key, "val");
	}

	@Test
	public void case_DELETE_ExistKey_OKStatus() {
		String key = "TestKey-random_" + TimeMillis.INSTANCE.get() + "" + Math.random();
		redisClient.set(key, "val");
		Assert.assertEquals(1, redisClient.del(key));
	}

	@Test
	public void case_GET_NotExistKey_Null() {
		String key = "TestKey-random_" + TimeMillis.INSTANCE.get() + "" + Math.random();
		Assert.assertNull(redisClient.get(key));
	}

	@Test
	public void case_GET_ExistsKey_Value() {
		String key = "TestKey-random_" + TimeMillis.INSTANCE.get() + "" + Math.random();
		String value = "val";

		redisClient.set(key, value);
		Assert.assertEquals(value, redisClient.get(key));
	}

	@Test
	public void case_STRLEN_LargeKey_Equal() {
		String key = "TestKey-random_" + TimeMillis.INSTANCE.get() + "" + Math.random();
		byte[] largeBytes = new byte[1024 * 1024 * 5];//1M
		redisClient.set(key, new String(largeBytes));
		Assert.assertEquals(largeBytes.length, redisClient.strlen(key));
	}

	@Test
	public void case_APPEND_NotExistKey_ValueSet() {
		String key = "TestKey-random_" + TimeMillis.INSTANCE.get() + "" + Math.random();
		String value = "val";
		Assert.assertEquals(value.length(), redisClient.append(key, value));
	}

	@Test
	public void case_APPEND_ExistKey_ValueAppend() {
		String key = "TestKey-random_" + TimeMillis.INSTANCE.get() + "" + Math.random();
		String value = "val";
		Assert.assertEquals(value.length(), redisClient.append(key, value));
		Assert.assertEquals(value.length() * 2, redisClient.append(key, value));
	}
}
