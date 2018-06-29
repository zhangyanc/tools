package pers.zyc.tools.redis.client;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author zhangyancheng
 */
public class ClientConfigTest {

	@Test
	public void case_StringConstruction_1_Success() {
		ClientConfig config = new ClientConfig("redis://password@localhost:6379/1" +
				"?connectionTimeout=2000&requestTimeout=3000&netWorkers=4");
		Assert.assertFalse(config.isSsl());
		Assert.assertEquals("localhost", config.getHost());
		Assert.assertTrue(6379 == config.getPort());
		Assert.assertEquals("password", config.getPassword());
		Assert.assertTrue(1 == config.getDb());
		Assert.assertTrue(2000 == config.getConnectionTimeout());
		Assert.assertTrue(3000 == config.getRequestTimeout());
		Assert.assertTrue(4 == config.getNetWorkers());
	}
}
