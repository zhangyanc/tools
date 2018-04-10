package pers.zyc.tools.zkclient;

import org.apache.zookeeper.KeeperException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
public class ReCreatorTest extends BaseClientTest {

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

	@Before
	public void setUp() throws Exception {
		super.setUp();

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setSyncStart(true);
		clientConfig.setConnectStr(CONNECT_STRING);
		clientConfig.setSessionTimeout(30000);

		createZKClient(clientConfig);

		zkSwitch.open();
		zkClient.start();

		cli.executeLine("rmr /zkclient");
		cli.executeLine("create /zkclient a");
	}

	@Test
	public void case_Recreate_unSeq() throws Exception {
		String testPath = "/zkclient/unSeq";

		TestRecreateListener testRecreateListener = new TestRecreateListener();
		zkClient.createEphemeral(testPath, new byte[0], false, testRecreateListener);

		makeCurrentZkClientSessionExpire();

		testRecreateListener.latch.await();
		Assert.assertEquals(testPath, testRecreateListener.recreatePathQueue.element());
	}

	@Test
	public void case_Recreate_seq() throws Exception {
		String testPath = "/zkclient/seq";

		TestRecreateListener testRecreateListener = new TestRecreateListener();
		String actualPath = zkClient.createEphemeral(testPath, new byte[0], true, testRecreateListener);
		logger.info(actualPath + " created!");

		int seq = Integer.parseInt(actualPath.substring(testPath.length()));

		makeCurrentZkClientSessionExpire();

		testRecreateListener.latch.await();

		String recreatePath = testRecreateListener.recreatePathQueue.element();
		logger.info(recreatePath + " recreated!");

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

		makeCurrentZkClientSessionExpire();

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

		makeCurrentZkClientSessionExpire();

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

		makeCurrentZkClientSessionExpire();

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
