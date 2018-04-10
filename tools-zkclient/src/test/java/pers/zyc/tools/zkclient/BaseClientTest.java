package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.zkclient.listener.ConnectionListener;

import java.util.concurrent.CountDownLatch;

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

	final Logger logger = LoggerFactory.getLogger(getClass());

	ZKSwitch zkSwitch;
	ZKClient zkClient;
	ZKCli cli;

	@Before
	public void setUp() throws Exception {
		zkSwitch = new ZKSwitch("E:/Tools/zookeeper-3.4.10");
		cli = new ZKCli(CONNECT_STRING);
	}

	@After
	public void tearDown() {
		zkSwitch.close();
		cli.close();
		if (zkClient != null && zkClient.isRunning()) {
			zkClient.stop();
		}
	}

	void createZKClient(ClientConfig config) {
		zkClient = new ZKClient(config);
	}

	void makeCurrentZkClientSessionExpire() throws Exception {
		long sessionId = zkClient.getZooKeeper().getSessionId();
		byte[] sessionPwd = zkClient.getZooKeeper().getSessionPasswd();

		final CountDownLatch connectedLatch = new CountDownLatch(1);
		ZooKeeper zooKeeper = new ZooKeeper(CONNECT_STRING, 30000, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				if (event.getState() == Event.KeeperState.SyncConnected) {
					connectedLatch.countDown();
				}
			}
		}, sessionId, sessionPwd);

		connectedLatch.await();
		zooKeeper.close();
	}

	@Test
	public void case_mockSessionExpire() throws Exception {
		zkSwitch.open();

		ClientConfig config = new ClientConfig();
		config.setSyncStart(true);
		createZKClient(config);

		zkClient.start();

		final CountDownLatch newSessionLatch = new CountDownLatch(2);

		zkClient.addListener(new DebugConnectionListener(new ConnectionListener() {

			@Override
			public void onConnected(boolean newSession) {
				if (newSession) {
					newSessionLatch.countDown();
				}
			}

			@Override
			public void onDisconnected(boolean sessionClosed) {
				if (sessionClosed) {
					newSessionLatch.countDown();
				}
			}
		}));

		logger.info("Make session expire");
		makeCurrentZkClientSessionExpire();

		newSessionLatch.await();
	}
}
