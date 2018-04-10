package pers.zyc.tools.zkclient;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pers.zyc.tools.zkclient.listener.ConnectionListener;

import java.util.concurrent.SynchronousQueue;

/**
 * @author zhangyancheng
 */
public class ZKConnectorTest {
	private static final int SESSION_TIMEOUT = 30000;

	enum ConnectionEvent {
		CONNECTED, RECONNECTED, SUSPEND, SESSION_CLOSED
	}

	private static class TestConnectorConnectionListener implements ConnectionListener {

		SynchronousQueue<ConnectionEvent> events = new SynchronousQueue<>();

		@Override
		public void onConnected(boolean newSession) {
			try {
				events.put(newSession ? ConnectionEvent.CONNECTED : ConnectionEvent.RECONNECTED);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onDisconnected(boolean sessionClosed) {
			try {
				events.put(sessionClosed ? ConnectionEvent.SESSION_CLOSED : ConnectionEvent.SUSPEND);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		ConnectionEvent acquireEvent() throws InterruptedException {
			return events.take();
		}
	}

	private ZKSwitch zkSwitch;
	private ZKConnector connector;
	private TestConnectorConnectionListener testListener;

	@Before
	public void setUp() throws InterruptedException {
		connector = new ZKConnector("localhost:2181", SESSION_TIMEOUT);
		zkSwitch = new ZKSwitch("E:/Tools/zookeeper-3.4.10");
		testListener = new TestConnectorConnectionListener();
		zkSwitch.open();
	}

	@After
	public void tearDown() {
		connector.stop();
		zkSwitch.close();
	}

	/**
	 * 测试未启动状态
	 */
	@Test
	public void case_unStarted() throws Exception {
		Assert.assertNull(connector.getZooKeeper());
		Assert.assertFalse(connector.isConnected());
	}

	/**
	 * 测试事件发布主流程
	 *
	 * 测试步骤:
	 *        1. 启动zk服务
	 *        2. 添加监听器(不会收到事件)
	 *        3. 启动connector(监听到CONNECTED事件)
	 *        4. 关闭zk(监听到DISCONNECTED事件)
	 *        5. 在sessionTimeout之前重启zk(监听到RECONNECTED事件)
	 *        6. 关闭zk(监听到DISCONNECTED事件)
	 *        7. 在sessionTimeout之前未重启zk(监听到SESSION_CLOSED事件)
	 *        8. 重启zk(监听到CONNECTED事件)
	 *        9. 关闭connector(不会受到事件)
	 */
	@Test
	public void case_mainFlow() throws Exception {
		connector.addListener(new BaseClientTest.DebugConnectionListener(testListener));

		connector.start();
		Assert.assertEquals(ConnectionEvent.CONNECTED, testListener.acquireEvent());
		Assert.assertTrue(connector.isConnected());

		zkSwitch.close();
		Assert.assertEquals(ConnectionEvent.SUSPEND, testListener.acquireEvent());
		Assert.assertFalse(connector.isConnected());

		Thread.sleep(Math.abs((long) (Math.random() * SESSION_TIMEOUT) - 1000));
		zkSwitch.open();
		Assert.assertEquals(ConnectionEvent.RECONNECTED, testListener.acquireEvent());
		Assert.assertTrue(connector.isConnected());

		zkSwitch.close();
		Assert.assertEquals(ConnectionEvent.SUSPEND, testListener.acquireEvent());
		Assert.assertFalse(connector.isConnected());


		Assert.assertEquals(ConnectionEvent.SESSION_CLOSED, testListener.acquireEvent());
		Assert.assertFalse(connector.isConnected());

		zkSwitch.open();
		Assert.assertEquals(ConnectionEvent.CONNECTED, testListener.acquireEvent());
		Assert.assertTrue(connector.isConnected());

		connector.stop();
		Assert.assertFalse(connector.isConnected());
	}
}
