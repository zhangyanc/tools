package pers.zyc.tools.redis.client;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pers.zyc.tools.redis.client.exception.ErrorType;
import pers.zyc.tools.redis.client.exception.ServerRespondException;
import pers.zyc.tools.utils.TimeMillis;

/**
 * Nonstandard unit tests.
 *
 * @author zhangyancheng
 */
public class RedisClientTest {

	private RedisClient redisClient;

	@Before
	public void setUp() throws Exception {
		ClientConfig config = new ClientConfig("redis://localhost:6379");

		redisClient = new RedisClient(config);
	}

	@After
	public void tearDown() {
		redisClient.close();
	}

	@Test
	public void case_Set_normal_success() {
		String key = "TestKey-random_" + TimeMillis.INSTANCE.get() + "" + Math.random();

		Assert.assertEquals("OK", redisClient.set(key, "a"));
	}

	@Test
	public void case_Set_LargeStr_success() {
		String key = "TestKey-random_" + TimeMillis.INSTANCE.get() + "" + Math.random();

		byte[] largeStrBytes = new byte[1024 * 1024];//1M
		redisClient.set(key, new String(largeStrBytes));

		Assert.assertTrue(largeStrBytes.length == redisClient.strlen(key));
	}


	@Test
	public void case_Incr_WrongType_error() {
		String key = "TestKey-random_" + TimeMillis.INSTANCE.get() + "" + Math.random();
		redisClient.set(key, "a");

		try {
			redisClient.incr(key);
			Assert.fail();
		} catch (ServerRespondException sre) {
			Assert.assertEquals(ErrorType.ERROR, sre.getErrorType());
		}
	}

	@Test
	public void case_Increment_NotExistsKey_zeroReturn() {
		String key = "TestKey-INCR_" + TimeMillis.INSTANCE.get() + "" + Math.random();

		int t = 5;
		for (int i = 1; i <= t; i++) {
			Assert.assertTrue(redisClient.incr(key) == i);
		}

		Assert.assertTrue(redisClient.incrBy(key, -t) == 0);
	}

	@Test
	public void case_Exists_normal_success() {
		String key = "TestKey-SET_normal";
		Assert.assertTrue(redisClient.exists(key));
	}

	@Test
	public void case_Type_stringKey_success() {
		String key = "TestKey-SET_normal";
		Assert.assertEquals("string", redisClient.type(key));
	}

	@Test
	public void case_SocketClose() throws Exception {
		String key = "TestKey-SocketClose-" + System.currentTimeMillis() + "_" + Math.random();
		Assert.assertFalse(redisClient.exists(key));

		Thread.currentThread().join();
	}
}
