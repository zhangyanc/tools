package pers.zyc.tools.zkclient;

import org.apache.zookeeper.data.Stat;
import org.junit.Assert;
import org.junit.Test;
import pers.zyc.tools.utils.TimeMillis;
import pers.zyc.tools.zkclient.listener.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author zhangyancheng
 */
public class ZKClientTest extends BaseClientTest {

	private static final String BASE_PATH = "/zkclient";
	private final String listenerTestPath = BASE_PATH + "/listenerTest" + (long) (Math.random() * 10000);

	@Test
	public void case_UnConnectedClient() throws Exception {
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setConnectStr(CONNECT_STRING);
		createZKClient(clientConfig);

		try {
			zkClient.exists(BASE_PATH);
			Assert.fail();
		} catch (ClientException e) {
			Assert.assertEquals("ZooKeeper is not connected!", e.getMessage());
		}
	}

	@Test
	public void case_syncStart() throws Exception {
		ClientConfig clientConfig = new ClientConfig();
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
		long beforeTime = TimeMillis.INSTANCE.get();
		//zkClient.start();
		long endTime = TimeMillis.INSTANCE.get();

		Assert.assertTrue(zkClient.isConnected());
		//ZooKeeper Server进程启动需要时间, 使用5000ms容错
		Assert.assertTrue((endTime - beforeTime - sleep) < 5000);
	}

	@Test
	public void case_stop_StopEventPublish() throws Exception {
		zkSwitch.open();

		ClientConfig clientConfig = new ClientConfig();
		createZKClient(clientConfig);

		final CountDownLatch cdl = new CountDownLatch(1);

		zkClient.addListener(new ClientDestroyListener() {

			@Override
			public void onDestroy() {
				cdl.countDown();
			}
		});
		zkClient.destroy();

		if (!cdl.await(2000, TimeUnit.MILLISECONDS)) {
			Assert.fail("Not stopped!");
		}
	}

	@Test
	public void case_AddConnectionListener() throws Exception {
		zkSwitch.open();

		ClientConfig clientConfig = new ClientConfig();
		createZKClient(clientConfig);

		final Semaphore events = new Semaphore(0);

		zkClient.addConnectionListener(new DebugConnectionListener(new ConnectionListener() {

			Set<Integer> set = new HashSet<>();

			@Override
			public void onConnected(boolean newSession) {
				if (set.add(newSession ? 1 : 2)) {
					events.release();
				}
			}

			@Override
			public void onDisconnected(boolean sessionClosed) {
				if (set.add(sessionClosed ? 3 : 4)) {
					events.release();
				}
			}
		}));

		makeCurrentZkClientSessionExpire();
		events.acquire(3);
		Assert.assertTrue(events.drainPermits() == 0);

		zkSwitch.close();
		zkSwitch.open();
		events.acquire(1);

		Assert.assertTrue(events.availablePermits() == 0);
	}

	/**
	 * 测试存在监听器
	 */
	@Test
	public void case_ExistsListener() throws Exception {
		zkSwitch.open();

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setConnectStr(CONNECT_STRING);

		createZKClient(clientConfig);

		cli.executeLine("rmr " + BASE_PATH);//清空测试目录
		cli.executeLine("create " + BASE_PATH + " a");

		final Semaphore events = new Semaphore(0);

		NodeEventWatcher nodeEventWatcher = zkClient.createNodeEventWatcher(listenerTestPath);
		Assert.assertEquals(listenerTestPath, nodeEventWatcher.getWatchedNodePath());

		nodeEventWatcher.addListener(new ExistsEventListener() {
			@Override
			public void onNodeCreated(String path, Stat stat) {
				events.release();
			}

			@Override
			public void onNodeDeleted(String path) {
				events.release();
			}
		});

		//等待内部线程启动
		Thread.sleep(200);

		String[] opts = {
				"create " + listenerTestPath + " a",
				"delete " + listenerTestPath,
				"create " + listenerTestPath + " a"
		};

		for (String opt : opts) {
			cli.executeLine(opt);
			if (!events.tryAcquire(2000, TimeUnit.MILLISECONDS)) {
				Assert.fail("Wait event timeout!");
			}
		}

		Assert.assertTrue(events.availablePermits() == 0);
	}

	@Test
	public void case_DataListener() throws Exception {
		zkSwitch.open();

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setConnectStr(CONNECT_STRING);

		createZKClient(clientConfig);

		cli.executeLine("rmr " + BASE_PATH);//清空测试目录
		cli.executeLine("create " + BASE_PATH + " a");

		final SynchronousQueue<String> events = new SynchronousQueue<>();

		NodeEventWatcher nodeEventWatcher = zkClient.createNodeEventWatcher(listenerTestPath);
		Assert.assertEquals(listenerTestPath, nodeEventWatcher.getWatchedNodePath());

		nodeEventWatcher.addListener(new DataEventListener() {

			@Override
			public void onDataChanged(String path, Stat stat, byte[] data) {
				try {
					events.put(new String(data));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onNodeDeleted(String path) {
				try {
					events.put(path);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		//等待reactor内部线程启动
		Thread.sleep(200);
		cli.executeLine("create " + listenerTestPath + " F");
		//等待reactor内部线程获取到初始值后才能触发后续变更
		Thread.sleep(200);

		String[] datas = {"dataA", "dataB", "dataB"};

		for (String data : datas) {
			cli.executeLine("set " + listenerTestPath + " " + data);
			Assert.assertEquals(data, events.take());
		}

		cli.executeLine("delete " + listenerTestPath);
		Assert.assertEquals(listenerTestPath, events.take());
	}

	@Test
	public void case_ChildrenListener() throws Exception {
		zkSwitch.open();

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setConnectStr(CONNECT_STRING);

		createZKClient(clientConfig);

		cli.executeLine("rmr " + BASE_PATH);//清空测试目录
		cli.executeLine("create " + BASE_PATH + " a");

		final SynchronousQueue<List<String>> childrenEvents = new SynchronousQueue<>();

		NodeEventWatcher nodeEventWatcher = zkClient.createNodeEventWatcher(listenerTestPath);
		Assert.assertEquals(listenerTestPath, nodeEventWatcher.getWatchedNodePath());

		nodeEventWatcher.addListener(new ChildrenEventListener() {

			@Override
			public void onChildrenChanged(String path, List<String> children) {
				try {
					childrenEvents.put(new ArrayList<>(children));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onNodeDeleted(String path) {
				try {
					childrenEvents.put(new ArrayList<String>());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		//等待reactor内部线程启动
		Thread.sleep(200);
		cli.executeLine("create " + listenerTestPath + " a");
		//等待reactor内部线程获取到初始值后才能触发后续变更
		Thread.sleep(200);

		final List<String> children = new ArrayList<String>() {
			{
				add("child1");
				add("child2");
				add("child3");
			}
		};

		for (int i = 0; i < children.size(); i++) {
			cli.executeLine("create " + listenerTestPath + "/" + children.get(i) + " a");
			List<String> cn = childrenEvents.take();
			cn.removeAll(children.subList(0, i + 1));
			Assert.assertTrue(cn.isEmpty());
		}

		for (int i = children.size() - 1; i >= 0; i--) {
			cli.executeLine("delete " + listenerTestPath + "/" + children.get(i));
			List<String> cn = childrenEvents.take();
			cn.removeAll(children.subList(0, i));
			Assert.assertTrue(cn.isEmpty());
		}

		cli.executeLine("delete " + listenerTestPath);
		Assert.assertTrue(childrenEvents.take().isEmpty());
	}

	/**
	 * TODO: 待补充
	 */
	@Test
	public void case_retry_unRetry() throws Exception {
		/*zkSwitch.open();

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setConnectStr(CONNECT_STRING);
		clientConfig.setUseRetry(false);
		clientConfig.setSyncStart(true);

		createZKClient(clientConfig);
		zkClient.start();

		zkSwitch.close();*/
	}

	private class RetryTestableZKClient extends ZKClient {

		public RetryTestableZKClient(ClientConfig config) {
			super(config);
		}

	}

}
