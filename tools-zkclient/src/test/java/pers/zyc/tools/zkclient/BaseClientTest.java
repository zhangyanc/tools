package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.zkclient.listener.ConnectionListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangyancheng
 */
public class BaseClientTest {

	static abstract class ConnectionListenerWrapper implements ConnectionListener {
		private final ConnectionListener target;

		ConnectionListenerWrapper(ConnectionListener target) {
			this.target = target;
		}

		@Override
		public void onConnected(boolean newSession) {
			target.onConnected(newSession);
		}

		@Override
		public void onDisconnected(boolean sessionClosed) {
			target.onDisconnected(sessionClosed);
		}
	}

	static class DebugConnectionListener extends ConnectionListenerWrapper {

		private static final Logger LOGGER = LoggerFactory.getLogger(DebugConnectionListener.class);

		DebugConnectionListener(ConnectionListener target) {
			super(target);
		}

		@Override
		public void onConnected(boolean newSession) {
			LOGGER.debug("Connected: newSession[{}]", newSession);
			super.onConnected(newSession);
		}

		@Override
		public void onDisconnected(boolean sessionClosed) {
			LOGGER.debug("Disconnected: sessionClosed[{}]", sessionClosed);
			super.onDisconnected(sessionClosed);
		}
	}

	/**
	 * 需要预先创建/test节点, 否则无法执行测试
	 */
	static final String CONNECT_STRING = "localhost:2181/test";
	static final int SESSION_TIMEOUT = 30000;

	//ZooKeeper Server进程启动需要时间, 使用3000ms容错
	static final int ZK_SERVER_START_TIMEOUT = 3000;

	final Logger logger = LoggerFactory.getLogger(getClass());

	ZKSwitch zkSwitch;
	ZKClient zkClient;
	ZKCli cli;

	void createZKClient() {
		zkClient = new ZKClient(CONNECT_STRING, SESSION_TIMEOUT);
	}

	void createZKClient(String connectStr, int sessionTimeout) {
		zkClient = new ZKClient(connectStr, sessionTimeout);
	}

	void createZKClient(String connectStr, int sessionTimeout, int retryTimes, int retryPerWaitTimeout) {
		zkClient = new ZKClient(connectStr, sessionTimeout, retryTimes, retryPerWaitTimeout);
	}

	void createSwitch() throws InterruptedException {
		createSwitch("E:/Tools/zookeeper-3.4.10");
	}

	void createSwitch(String zkDir) throws InterruptedException {
		zkSwitch = new ZKSwitch(zkDir);
	}

	static void makeZooKeeperSessionExpire(ZooKeeper zooKeeper) throws Exception {
		final CountDownLatch connectedLatch = new CountDownLatch(1);
		ZooKeeper zk = new ZooKeeper(CONNECT_STRING, 30000, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				if (event.getState() == Event.KeeperState.SyncConnected) {
					connectedLatch.countDown();
				}
			}
		}, zooKeeper.getSessionId(), zooKeeper.getSessionPasswd());
		connectedLatch.await();
		zk.close();
	}

	void makeCurrentZkClientSessionExpire() throws Exception {
		makeZooKeeperSessionExpire(zkClient.getZooKeeper());
	}

	@After
	public void tearDown() {
		if (zkClient != null) {
			zkClient.destroy();
		}
		if (cli != null) {
			cli.close();
		}
		if (zkSwitch != null) {
			zkSwitch.close();
		}
	}

	@Test
	public void case0_waitToConnected() throws Exception {
		createZKClient();

		Assert.assertFalse(zkClient.waitToConnected(ZK_SERVER_START_TIMEOUT, TimeUnit.MILLISECONDS));

		createSwitch();
		zkSwitch.open();

		Assert.assertTrue(zkClient.waitToConnected(ZK_SERVER_START_TIMEOUT, TimeUnit.MILLISECONDS));
	}

	@Test
	public void case0_mockSessionExpire() throws Exception {
		createSwitch();
		zkSwitch.open();

		createZKClient();
		Assert.assertTrue(zkClient.waitToConnected(ZK_SERVER_START_TIMEOUT, TimeUnit.MILLISECONDS));


		final CountDownLatch newSessionLatch = new CountDownLatch(2);

		zkClient.addConnectionListener(new DebugConnectionListener(new ConnectionListener() {

			@Override
			public void onConnected(boolean newSession) {
			}

			@Override
			public void onDisconnected(boolean sessionClosed) {
				newSessionLatch.countDown();
			}
		}));

		logger.info("Make session expire");
		makeCurrentZkClientSessionExpire();

		newSessionLatch.await();
		Assert.assertFalse(zkClient.isConnected());
	}
}
