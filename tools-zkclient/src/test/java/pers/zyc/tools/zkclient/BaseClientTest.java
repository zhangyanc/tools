package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author zhangyancheng
 */
public class BaseClientTest {

	/**
	 * 需要预先创建/test节点, 否则无法执行测试
	 */
	static final String CONNECT_STRING = "localhost:2181/test";

	final Logger logger = LoggerFactory.getLogger(getClass());

	ZKSwitch zkSwitch;
	ZKClient zkClient;
	ZKCli cli;

	@Before
	public void setUp() throws IOException {
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
}
