package pers.zyc.tools.redis.client;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author zhangyancheng
 */
public class RedisClientTest {

	private ConnectionPool connectionPool;
	private RedisClient redisClient;

	@Before
	public void setUp() throws Exception {
		connectionPool = new ConnectionPool("redis://172.25.45.240:5274");
		connectionPool.start();

		redisClient = new CustomRedisClient(connectionPool);
	}

	@After
	public void tearDown() {
		connectionPool.stop();
	}

	@Test
	public void case_SET_normal_success() {
		String key = "TestKey-SET_normal";
		String val = "val";

		String setResp = redisClient.set(key, val);
		Assert.assertEquals("OK", setResp);

		String getResp = redisClient.get(key);
		Assert.assertEquals(val, getResp);
	}


	@Test
	public void case_Increment_WrongType_error() {
		String key = "TestKey-SET_normal";
		try {
			redisClient.incr(key);
			Assert.fail();
		} catch (RedisClientException rce) {
			Assert.assertTrue(rce.getMessage().contains("ERR"));
		}
	}
}
