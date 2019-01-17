package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
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
	/**
	 * 本机zookeeper安装目录
	 */
	static final String ZK_DIR = "E:/Tools/zookeeper-3.4.10";

	static final int SESSION_TIMEOUT = 30000;

	//ZooKeeper Server进程启动需要时间, 使用3000ms容错
	static final int ZK_SERVER_START_TIMEOUT = 3000;

	final Logger logger = LoggerFactory.getLogger(getClass());

	static ZKClient createZKClient() {
		return new ZKClient(CONNECT_STRING, SESSION_TIMEOUT);
	}

	static ZKClient createZKClient(String connectStr, int sessionTimeout) {
		return new ZKClient(connectStr, sessionTimeout);
	}

	static ZKClient createZKClient(String connectStr, int sessionTimeout, int retryTimes, int retryPerWaitTimeout) {
		return new ZKClient(connectStr, sessionTimeout, retryTimes, retryPerWaitTimeout);
	}

	static ZKCli createCli() throws Exception {
		return new ZKCli(CONNECT_STRING);
	}

	static ZKSwitch createSwitch() throws InterruptedException {
		return createSwitch(ZK_DIR);
	}

	static ZKSwitch createSwitch(String zkDir) throws InterruptedException {
		return new ZKSwitch(zkDir);
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

	static void makeCurrentZkClientSessionExpire(ZKClient zkClient) throws Exception {
		makeZooKeeperSessionExpire(zkClient.getZooKeeper());
	}
}
