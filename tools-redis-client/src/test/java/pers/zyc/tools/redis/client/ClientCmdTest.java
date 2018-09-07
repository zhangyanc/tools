package pers.zyc.tools.redis.client;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author zhangyancheng
 */
public class ClientCmdTest {

	private static final String TESTABLE_REDIS_SERVER = "172.25.45.240:5385";

	@Test
	public void case_CLIENT_GET_SET_NAME() throws Exception {
		ClientConfig config = new ClientConfig("redis://" +TESTABLE_REDIS_SERVER +
				"?minConnectionIdle=1&maxConnectionTotal=1");
		//只有一个连接
		ConnectionPool connectionPool = new ConnectionPool(config);

		try {
			Assert.assertNull(connectionPool.getConnection().getClientName());

			String clientName = "cn0";
			connectionPool.getConnection().setClientName(clientName);

			Assert.assertEquals(clientName, connectionPool.getConnection().getClientName());
		} finally {
			connectionPool.stop();
		}
	}

	@Test
	public void case_CLIENT_LIST() throws Exception {
		ClientConfig config = new ClientConfig("redis://" +TESTABLE_REDIS_SERVER +
				"?minConnectionIdle=1&maxConnectionTotal=1");
		//只有一个连接
		ConnectionPool connectionPool = new ConnectionPool(config);

		try {
			String clientName = "cn0";
			connectionPool.getConnection().setClientName(clientName);

			List<Map<String, String>> list = connectionPool.getConnection().clientList();


			Map<String, String> currentClient = null;
			for (Map<String, String> infoMap : list) {
				if (clientName.equals(infoMap.get("name"))) {
					currentClient = infoMap;
					break;
				}
			}
			//当前连接的最后一次请求未client
			Assert.assertNotNull(currentClient);
			Assert.assertEquals("client", currentClient.get("cmd"));
		} finally {
			connectionPool.stop();
		}
	}
}
