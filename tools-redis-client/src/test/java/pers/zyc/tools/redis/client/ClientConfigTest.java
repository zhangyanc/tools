package pers.zyc.tools.redis.client;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author zhangyancheng
 */
public class ClientConfigTest {

	@Test
	public void case_StringConstruction_1_Success() {
		ClientConfig config = new ClientConfig("redis://localhost:6379");
		Assert.assertFalse(config.isSsl());
		Assert.assertEquals("localhost", config.getHost());
		Assert.assertTrue(6379 == config.getPort());
	}
}
