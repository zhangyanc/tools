package pers.zyc.tools.redis.client;

import org.junit.Assert;
import org.junit.Test;
import pers.zyc.tools.redis.client.exception.ErrorType;
import pers.zyc.tools.redis.client.exception.ServerRespondException;
import pers.zyc.tools.redis.client.request.connection.Echo;

/**
 * @author zhangyancheng
 */
public class ConnectionCmdTest {

	private static final String TESTABLE_REDIS_SERVER = "172.25.45.240:5385";

	@Test
	public void case_QUIT() throws Exception {
		ClientConfig config = new ClientConfig("redis://" + TESTABLE_REDIS_SERVER +
				"?minConnectionIdle=0&maxConnectionTotal=1");
		//最少0个连接, 最多1个连接
		ConnectionPool connectionPool = new ConnectionPool(config);

		try {
			Connection connection = connectionPool.getConnection();
			Assert.assertEquals(1, connectionPool.getInternalPool().getNumActive());

			connection.quit();
			Assert.assertEquals(0, connectionPool.getInternalPool().getNumActive());
		} finally {
			connectionPool.stop();
		}
	}

	@Test
	public void case_PING() throws Exception {
		ClientConfig config = new ClientConfig("redis://" +TESTABLE_REDIS_SERVER +
				"?minConnectionIdle=1&maxConnectionTotal=1");
		ConnectionPool connectionPool = new ConnectionPool(config);

		try {
			connectionPool.getConnection().ping();
		} finally {
			connectionPool.stop();
		}
	}

	@Test
	public void case_ECHO() throws Exception {
		ClientConfig config = new ClientConfig("redis://" +TESTABLE_REDIS_SERVER +
				"?minConnectionIdle=1&maxConnectionTotal=1");
		ConnectionPool connectionPool = new ConnectionPool(config);

		try {
			String msg = "msg";
			Assert.assertEquals(msg, connectionPool.getConnection().send(new Echo(msg)).get());
		} finally {
			connectionPool.stop();
		}
	}

	@Test
	public void case_AUTH() throws Exception {
		ClientConfig config = new ClientConfig("redis://" +TESTABLE_REDIS_SERVER +
				"?minConnectionIdle=1&maxConnectionTotal=1");
		ConnectionPool connectionPool = new ConnectionPool(config);

		try {
			connectionPool.getConnection().auth("pwd");
			Assert.fail("Auth should failed!");
		} catch (ServerRespondException sre) {
			Assert.assertEquals(ErrorType.ERROR, sre.getErrorType());
		} finally {
			connectionPool.stop();
		}
	}
}
