package pers.zyc.tools.zkclient;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.zkclient.listener.ConnectionListenerAdapter;
import pers.zyc.tools.zkclient.listener.RecreateListener;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

/**
 * @author zhangyancheng
 */
public class ReCreatorTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReCreatorTest.class);

	/**
	 * 需要预先创建/test节点, 否则无法执行测试
	 */
	private static final String CONNECT_STRING = "localhost:2181/test";

	class TestRecreateListener implements RecreateListener {

		CountDownLatch latch;
		Exception exception;
		Queue<String> recreatePathQueue = new LinkedList<>();

		TestRecreateListener() {
			this.latch = new CountDownLatch(1);
		}

		TestRecreateListener(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public void onRecreateSuccessful(String path, String actualPath) {
			recreatePathQueue.add(actualPath);
			latch.countDown();
		}

		@Override
		public void onRecreateFailed(String path, Exception exception) {
			this.exception = exception;
			latch.countDown();
		}
	}

	private ZKSwitch zkSwitch;
	private ZKClient zkClient;
	private ZKCli cli;

	@Before
	public void setUp() throws Exception {
		zkSwitch = new ZKSwitch("E:/Tools/zookeeper-3.4.10");

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setSyncStart(true);
		clientConfig.setConnectStr(CONNECT_STRING);
		clientConfig.setSessionTimeout(30000);

		zkClient = new ZKClient(clientConfig);

		zkSwitch.open();
		zkClient.start();

		cli = new ZKCli(CONNECT_STRING);
		cli.executeLine("rmr /zkclient");
		cli.executeLine("create /zkclient a");
	}

	@After
	public void tearDown() {
		zkClient.stop();
		zkSwitch.close();
	}

	private void makeZkClientSessionExpired() throws Exception {

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
	public void case_Recreate_unSeq() throws Exception {
		String testPath = "/zkclient/unSeq";

		TestRecreateListener testRecreateListener = new TestRecreateListener();
		zkClient.createEphemeral(testPath, new byte[0], false, testRecreateListener);

		makeZkClientSessionExpired();

		testRecreateListener.latch.await();
		Assert.assertEquals(testPath, testRecreateListener.recreatePathQueue.element());
	}

	@Test
	public void case_Recreate_seq() throws Exception {
		String testPath = "/zkclient/seq";

		TestRecreateListener testRecreateListener = new TestRecreateListener();
		String actualPath = zkClient.createEphemeral(testPath, new byte[0], true, testRecreateListener);
		LOGGER.info(actualPath + " created!");

		int seq = Integer.parseInt(actualPath.substring(testPath.length()));

		makeZkClientSessionExpired();

		testRecreateListener.latch.await();

		String recreatePath = testRecreateListener.recreatePathQueue.element();
		LOGGER.info(recreatePath + " recreated!");

		int recreateSeq = Integer.parseInt(recreatePath.substring(testPath.length()));

		Assert.assertTrue(recreateSeq > seq);
	}

	@Test
	public void case_UpdateData() throws Exception {
		String testPath = "/zkclient/upd";

		TestRecreateListener testRecreateListener = new TestRecreateListener();
		zkClient.createEphemeral(testPath, new byte[0], false, testRecreateListener);

		byte[] newData = new byte[]{1};
		zkClient.updateEphemeralData(testPath, newData);

		makeZkClientSessionExpired();

		testRecreateListener.latch.await();

		Assert.assertTrue(Arrays.equals(newData, zkClient.getData(testPath)));
	}

	@Test
	public void case_RecreateFailed_NodeExists() throws Exception {
		String testPath = "/zkclient/failed";

		TestRecreateListener testRecreateListener = new TestRecreateListener();
		zkClient.createEphemeral(testPath, new byte[0], false, testRecreateListener);

		new Thread() {
			@Override
			public void run() {
				final CountDownLatch latch = new CountDownLatch(1);

				zkClient.addListener(new ConnectionListenerAdapter() {

					@Override
					public void onDisconnected(boolean sessionClosed) {
						if (sessionClosed) {
							latch.countDown();
						}
					}
				});

				zkSwitch.close();
				try {
					latch.await();

					zkSwitch.open();//重启服务不会触发原session超时, 临时节点会被重新加载
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();

		testRecreateListener.latch.await();
		Assert.assertTrue(testRecreateListener.exception instanceof KeeperException.NodeExistsException);
	}

	@Test
	public void case_RecreateFailed_ParentNotExists() throws Exception {
		String testPath = "/zkclient/failed";

		TestRecreateListener testRecreateListener = new TestRecreateListener();
		zkClient.createEphemeral(testPath, new byte[0], false, testRecreateListener);

		cli.executeLine("rmr /zkclient");

		makeZkClientSessionExpired();

		testRecreateListener.latch.await();
		Assert.assertTrue(testRecreateListener.exception instanceof KeeperException.NoNodeException);
	}

	@Test
	public void case_DisconnectedWhenRecreating() throws Exception {
		List<String> testPaths = Arrays.asList("/zkclient/dwrA", "/zkclient/dwrB", "/zkclient/dwrC");

		final CountDownLatch latch = new CountDownLatch(testPaths.size());

		TestRecreateListener testRecreateListener = new TestRecreateListener(latch) {

			@Override
			public void onRecreateSuccessful(String path, String actualPath) {
				super.onRecreateSuccessful(path, actualPath);
				//Disconnected zk
				zkSwitch.close();
			}
		};

		zkClient.createEphemeral(testPaths.get(0), new byte[0], false, testRecreateListener);
		zkClient.createEphemeral(testPaths.get(1), new byte[0], false, testRecreateListener);
		zkClient.createEphemeral(testPaths.get(2), new byte[0], false, testRecreateListener);

		makeZkClientSessionExpired();

		zkClient.addListener(new ConnectionListenerAdapter() {

			@Override
			public void onDisconnected(boolean sessionClosed) {
				try {
					zkSwitch.open();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		latch.await();

		Assert.assertTrue(testRecreateListener.recreatePathQueue.size() == testPaths.size());
		testRecreateListener.recreatePathQueue.removeAll(testPaths);
		Assert.assertTrue(testRecreateListener.recreatePathQueue.isEmpty());
	}
}
