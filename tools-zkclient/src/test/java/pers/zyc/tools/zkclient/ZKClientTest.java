package pers.zyc.tools.zkclient;

import org.apache.zookeeper.data.Stat;
import org.junit.Assert;
import org.junit.Test;
import pers.zyc.tools.utils.TimeMillis;
import pers.zyc.tools.zkclient.listener.ChildrenEventListener;
import pers.zyc.tools.zkclient.listener.ConnectionListener;
import pers.zyc.tools.zkclient.listener.DataEventListener;
import pers.zyc.tools.zkclient.listener.ExistsEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhangyancheng
 */
public class ZKClientTest extends BaseClientTest {

	@Test
	public void case_UnStartClient() throws Exception {
		zkSwitch.open();

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setConnectStr(CONNECT_STRING);
		clientConfig.setSyncStart(true);

		createZKClient(clientConfig);

		try {
			zkClient.exists("/zkclient");
			Assert.fail();
		} catch (ClientException e) {
			Assert.assertEquals("ZooKeeper is not connected!", e.getMessage());
		}
	}

	@Test
	public void case_UnConnectedClient() throws Exception {
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setConnectStr(CONNECT_STRING);
		clientConfig.setSyncStart(false);
		createZKClient(clientConfig);
		zkClient.start();

		try {
			zkClient.exists("/zkclient");
			Assert.fail();
		} catch (ClientException e) {
			Assert.assertEquals("ZooKeeper is not connected!", e.getMessage());
		}
	}

	@Test
	public void case_syncStart() throws Exception {
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setSyncStart(true);
		createZKClient(clientConfig);

		final CyclicBarrier barrier = new CyclicBarrier(2);
		final int sleep = 8000;
		new Thread() {
			@Override
			public void run() {
				try {
					barrier.await();
					sleep(sleep);

					logger.debug("Launching zookeeper server");
					zkSwitch.open();
				} catch (InterruptedException | BrokenBarrierException e) {
					e.printStackTrace();
				}
			}
		}.start();
		barrier.await();
		long beforeTime = TimeMillis.get();
		zkClient.start();
		long endTime = TimeMillis.get();

		Assert.assertTrue(zkClient.isConnected());
		//ZooKeeper Server进程启动需要时间, 使用2000ms容错
		Assert.assertTrue((endTime - beforeTime - sleep) < 2000);
	}

	@Test
	public void case_AddConnectionListener() throws Exception {
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setSyncStart(false);
		createZKClient(clientConfig);
		zkClient.start();

		final CountDownLatch latch = new CountDownLatch(4);

		zkClient.addListener(new ConnectionListener() {

			boolean[][] bs = new boolean[2][2];

			@Override
			public void onConnected(boolean newSession) {
				int i = newSession ? 0 : 1;
				if (!bs[0][i]) {
					latch.countDown();
					bs[0][i] = true;
				}
			}

			@Override
			public void onDisconnected(boolean sessionClosed) {
				int i = sessionClosed ? 0 : 1;
				if (!bs[1][i]) {
					latch.countDown();
					bs[1][i] = true;
				}
			}
		});

		zkSwitch.open();
		makeCurrentZkClientSessionExpire();
		zkSwitch.close();
		zkSwitch.open();
		zkSwitch.close();

		Thread.sleep(clientConfig.getSessionTimeout());
		//Assert.assertTrue(events.get() == 5);
	}

	/**
	 * 测试存在监听器
	 */
	@Test
	public void case_ExistsListener() throws Exception {
		zkSwitch.open();

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setConnectStr(CONNECT_STRING);
		clientConfig.setSyncStart(true);

		createZKClient(clientConfig);
		zkClient.start();

		cli.executeLine("rmr /zkclient");//清空测试目录
		cli.executeLine("create /zkclient a");

		final AtomicInteger events = new AtomicInteger();
		zkClient.addListener("/zkclient/exists", new ExistsEventListener() {
			@Override
			public void onNodeCreated(String path, Stat stat) {
				events.getAndIncrement();
			}

			@Override
			public void onNodeDeleted(String path) {
				events.getAndIncrement();
			}
		});

		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					sleep(1000);
					cli.executeLine("create /zkclient/exists a");
					sleep(1000);
					cli.executeLine("delete /zkclient/exists");
					sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		t.start();
		t.join();
		Assert.assertTrue(events.get() == 2);
	}

	@Test
	public void case_DataListener() throws Exception {
		zkSwitch.open();

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setConnectStr(CONNECT_STRING);
		clientConfig.setSyncStart(true);

		createZKClient(clientConfig);
		zkClient.start();

		final ZKCli cli = new ZKCli(CONNECT_STRING);
		cli.executeLine("rmr /zkclient");//清空测试目录
		cli.executeLine("create /zkclient a");

		final List<String> dataList = new ArrayList<String>() {
			{
				add("dataA");
				add("dataB");
				add("dataB");
			}
		};

		final List<String> received = new ArrayList<>();

		zkClient.addListener("/zkclient/data", new DataEventListener() {

			@Override
			public void onDataChanged(String path, Stat stat, byte[] data) {
				received.add(new String(data));
			}

			@Override
			public void onNodeDeleted(String path) {

			}
		});

		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					sleep(1000);
					cli.executeLine("create /zkclient/data F");
					sleep(1000);

					for (String data : dataList) {
						cli.executeLine("set /zkclient/data " + data);
						sleep(1000);
					}

					cli.executeLine("delete /zkclient/data");
					sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		t.start();
		t.join();
		Assert.assertEquals(dataList, received);
	}

	@Test
	public void case_ChildrenListener() throws Exception {
		zkSwitch.open();

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setConnectStr(CONNECT_STRING);
		clientConfig.setSyncStart(true);

		createZKClient(clientConfig);
		zkClient.start();

		final ZKCli cli = new ZKCli(CONNECT_STRING);
		cli.executeLine("rmr /zkclient");//清空测试目录
		cli.executeLine("create /zkclient a");

		final AtomicReference<List<String>> listenedChildren = new AtomicReference<>();
		zkClient.addListener("/zkclient/children", new ChildrenEventListener() {
			@Override
			public void onChildrenChanged(String path, List<String> children) {
				listenedChildren.set(children);
			}

			@Override
			public void onNodeDeleted(String path) {

			}
		});

		final List<String> children = new ArrayList<String>() {
			{
				add("child1");
				add("child2");
				add("child3");
			}
		};

		Thread t = new Thread() {

			@Override
			public void run() {
				try {
					sleep(1000);
					cli.executeLine("create /zkclient/children a");

					for (int i = 0; i < children.size() - 1; i++) {
						cli.executeLine("create /zkclient/children/" + children.get(i) + " a");
						sleep(1000);
					}

					zkSwitch.close();
					sleep(1000);
					zkSwitch.open();

					cli.executeLine("create /zkclient/children/" + children.get(children.size() - 1) + " a");
					sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		t.start();
		t.join();

		children.removeAll(listenedChildren.get());

		Assert.assertTrue(children.isEmpty());
	}

	@Test
	public void case_retry_unRetry() throws Exception {
		//TODO
	}
}
