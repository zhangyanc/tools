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
