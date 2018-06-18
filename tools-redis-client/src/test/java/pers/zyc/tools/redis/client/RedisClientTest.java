package pers.zyc.tools.redis.client;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Nonstandard unit tests.
 *
 * @author zhangyancheng
 */
public class RedisClientTest {

	private RedisClient redisClient;

	@Before
	public void setUp() throws Exception {
		redisClient = new RedisClient("redis://localhost:6379");
		redisClient.start();
	}

	@After
	public void tearDown() {
		redisClient.stop();
	}

	@Test
	public void case_Set_normal_success() {
		String key = "TestKey-SET_normal";
		String val = "val";

		String setResp = redisClient.set(key, val);
		Assert.assertEquals("OK", setResp);

		String getResp = redisClient.get(key);
		Assert.assertEquals(val, getResp);
	}

	@Test
	public void case_Set_LargeStr_success() {
		String key = "TestKey-SET_large";

		int len = 1024 * 1024;
		byte[] largeStrBytes = new byte[len];//1M
		Assert.assertEquals("OK", redisClient.set(key, new String(largeStrBytes)));

		Assert.assertTrue(len == redisClient.get(key).length());
		Assert.assertTrue(len == redisClient.strlen(key));
	}


	@Test
	public void case_Increment_WrongType_error() {
		String key = "TestKey-SET_normal";
		try {
			redisClient.incr(key);
			Assert.fail();
		} catch (RedisClientException rce) {
			rce.printStackTrace();
			Assert.assertTrue(rce.getMessage().contains("ERR"));
		}
	}

	@Test
	public void case_Increment_NotExistsKey_zeroReturn() {
		String key = "TestKey-INCR_normal";
		Assert.assertTrue(redisClient.incr(key) == 1);
		Assert.assertTrue(redisClient.incr(key) == 2);
		Assert.assertTrue(redisClient.incr(key) == 3);

		Assert.assertTrue(redisClient.incrBy(key, -3) == 0);
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
}
