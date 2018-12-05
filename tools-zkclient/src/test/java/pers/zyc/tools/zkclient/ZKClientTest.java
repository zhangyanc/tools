package pers.zyc.tools.zkclient;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pers.zyc.tools.zkclient.listener.*;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangyancheng
 */
public class ZKClientTest extends BaseClientTest {

	private static final String BASE_PATH = "/zkclient";
	private final String listenerTestPath = BASE_PATH + "/listenerTest" + (long) (Math.random() * 100000);

	@Before
	public void setUp() throws Exception {
		createSwitch();
		createCli();
	}

	@Test(expected = KeeperException.ConnectionLossException.class)
	public void case_UnConnectedClient() throws Exception {
		createZKClient("localhost:2181", SESSION_TIMEOUT);

		zkClient.exists(BASE_PATH);
		Assert.fail();
	}

	@Test
	public void case0_waitToConnected() throws Exception {
		createZKClient();

		Assert.assertFalse(zkClient.waitToConnected(ZK_SERVER_START_TIMEOUT, TimeUnit.MILLISECONDS));

		zkSwitch.open();

		Assert.assertTrue(zkClient.waitToConnected(ZK_SERVER_START_TIMEOUT, TimeUnit.MILLISECONDS));
	}

	@Test
	public void case0_mockSessionExpire() throws Exception {
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

	@Test
	public void case_stop_StopEventPublish() throws Exception {
		createZKClient();

		final CountDownLatch cdl = new CountDownLatch(1);

		zkClient.addListener(new ClientDestroyListener() {

			@Override
			public void onDestroy() {
				cdl.countDown();
			}
		});
		zkClient.destroy();

		if (!cdl.await(1000, TimeUnit.MILLISECONDS)) {
			Assert.fail("Not stopped!");
		}
	}

	@Test
	public void case_AddConnectionListener() throws Exception {
		zkSwitch.open();

		createZKClient();
		zkClient.waitToConnected(ZK_SERVER_START_TIMEOUT, TimeUnit.MILLISECONDS);

		final Semaphore events = new Semaphore(0);
		final Set<Integer> set = new HashSet<>();

		zkClient.addConnectionListener(new DebugConnectionListener(new ConnectionListener() {

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
		events.acquire(3);//disconnected、expire、new-session
		Assert.assertTrue(events.availablePermits() == 0);

		zkSwitch.close();
		zkSwitch.open();
		events.acquire(1);//re-connected

		Assert.assertTrue(events.availablePermits() == 0);
	}

	/**
	 * 测试存在监听器
	 */
	@Test
	public void case_ExistsListener() throws Exception {
		zkSwitch.open();

		createZKClient();

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

		zkClient.waitToConnected(ZK_SERVER_START_TIMEOUT, TimeUnit.MILLISECONDS);
		//等待NodeEventWatcher内部线程启动
		Thread.sleep(500);

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

		createZKClient();

		cli.executeLine("rmr " + BASE_PATH);//清空测试目录
		cli.executeLine("create " + BASE_PATH + " a");

		NodeEventWatcher nodeEventWatcher = zkClient.createNodeEventWatcher(listenerTestPath);
		Assert.assertEquals(listenerTestPath, nodeEventWatcher.getWatchedNodePath());

		final SynchronousQueue<String> events = new SynchronousQueue<>();
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

		zkClient.waitToConnected(ZK_SERVER_START_TIMEOUT, TimeUnit.MILLISECONDS);
		//等待NodeEventWatcher内部线程启动
		Thread.sleep(500);

		cli.executeLine("create " + listenerTestPath + " F");
		//等待NodeEventWatcher内部线程获取到初始值后才能触发后续变更
		Thread.sleep(500);

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

		createZKClient();

		cli.executeLine("rmr " + BASE_PATH);//清空测试目录
		cli.executeLine("create " + BASE_PATH + " a");


		NodeEventWatcher nodeEventWatcher = zkClient.createNodeEventWatcher(listenerTestPath);
		Assert.assertEquals(listenerTestPath, nodeEventWatcher.getWatchedNodePath());

		final SynchronousQueue<List<String>> childrenEvents = new SynchronousQueue<>();
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

		zkClient.waitToConnected(ZK_SERVER_START_TIMEOUT, TimeUnit.MILLISECONDS);
		//等待reactor内部线程启动
		Thread.sleep(500);
		cli.executeLine("create " + listenerTestPath + " a");
		//等待reactor内部线程获取到初始值后才能触发后续变更
		Thread.sleep(500);

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



	@Test
	public void case_create_unRetryCreatePERSISTENT_created() throws Exception {
		{
			zkSwitch.open();
			createZKClient();
			cli.executeLine("rmr " + BASE_PATH);//清空测试目录
			cli.executeLine("create " + BASE_PATH + " a");
			zkClient.waitToConnected(ZK_SERVER_START_TIMEOUT, TimeUnit.MILLISECONDS);
		}

		String testPath = BASE_PATH + "/test_create";
		zkClient.create(testPath, new byte[0], CreateMode.PERSISTENT);

		Stat stat = cli.getZooKeeper().exists(testPath, false);
		Assert.assertTrue(0 == stat.getEphemeralOwner());
	}

	@Test
	public void case_create_unRetryCreateEPHEMERAL_created() throws Exception {
		{
			zkSwitch.open();
			createZKClient();
			cli.executeLine("rmr " + BASE_PATH);//清空测试目录
			cli.executeLine("create " + BASE_PATH + " a");
			zkClient.waitToConnected(ZK_SERVER_START_TIMEOUT, TimeUnit.MILLISECONDS);
		}

		long clientSessionId = zkClient.getZooKeeper().getSessionId();

		String testPath = BASE_PATH + "/test_create";
		zkClient.create(testPath, new byte[0], CreateMode.EPHEMERAL);

		Stat stat = cli.getZooKeeper().exists(testPath, false);
		Assert.assertTrue(clientSessionId == stat.getEphemeralOwner());
	}

	@Test
	public void case_create_retryCreateSEQUENTIAL_idAppend() throws Exception {
		{
			zkSwitch.open();
			createZKClient(CONNECT_STRING, SESSION_TIMEOUT, 2, 3000);
			cli.executeLine("rmr " + BASE_PATH);//清空测试目录
			cli.executeLine("create " + BASE_PATH + " a");
			zkClient.waitToConnected(ZK_SERVER_START_TIMEOUT, TimeUnit.MILLISECONDS);
		}

		String testPath = BASE_PATH + "/test_create";
		String created = zkClient.create(testPath, new byte[0], CreateMode.PERSISTENT_SEQUENTIAL);

		String expected = testPath + zkClient.getClientId() + "0000000000";//第一个序列号是0
		Assert.assertEquals(expected, created);
	}

	@Test
	public void case_create_retryCreateSEQUENTIAL_idBytesAppend() throws Exception {
		{
			zkSwitch.open();
			createZKClient(CONNECT_STRING, SESSION_TIMEOUT, 2, 3000);
			cli.executeLine("rmr " + BASE_PATH);//清空测试目录
			cli.executeLine("create " + BASE_PATH + " a");
			zkClient.waitToConnected(ZK_SERVER_START_TIMEOUT, TimeUnit.MILLISECONDS);
		}

		String testPath = BASE_PATH + "/test_create";
		String created = zkClient.create(testPath, new byte[0], CreateMode.PERSISTENT_SEQUENTIAL);

		Stat stat = cli.getZooKeeper().exists(created, false);
		Assert.assertTrue(21 == stat.getDataLength());
	}

	private static byte[] appendIdentityData(byte[] idBytes, byte[] data, int rId) {
		byte[] appendedData = new byte[21 + data.length];
		ByteBuffer buf = ByteBuffer.wrap(appendedData);
		buf.put((byte) '^');//1 字节魔数
		buf.put(idBytes);	//16字节id
		buf.putInt(rId);	//4 字节请求id
		buf.put(data);
		return appendedData;
	}

	@Test
	public void case_setData_retrySetData_idBytesAppend() throws Exception {
		String testPath = BASE_PATH + "/test_setData";

		byte[] clientIdBytes;
		{
			zkSwitch.open();
			createZKClient(CONNECT_STRING, SESSION_TIMEOUT, 2, 3000);
			cli.executeLine("rmr " + BASE_PATH);//清空测试目录
			cli.executeLine("create " + BASE_PATH + " a");
			zkClient.waitToConnected(ZK_SERVER_START_TIMEOUT, TimeUnit.MILLISECONDS);
			cli.executeLine("create " + testPath + " a");

			UUID uuid = UUID.fromString(zkClient.getClientId());
			ByteBuffer buffer = ByteBuffer.allocate(16);
			buffer.putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
			clientIdBytes = buffer.array();
		}

		byte[] testData = new byte[4];
		ByteBuffer.wrap(testData).putInt(new Random().nextInt());
		zkClient.setData(testPath, testData, -1);

		byte[] expected = appendIdentityData(clientIdBytes, testData, 1);//第一个请求id是1

		byte[] data = cli.getZooKeeper().getData(testPath, null, null);
		Assert.assertArrayEquals(expected, data);
	}

	@Test
	public void case_getData_getData_idBytesRemoved() throws Exception {
		String testPath = BASE_PATH + "/test_getData";

		String realData = "realData.";
		String createdData = "^1111#1111#1111#1111#" + realData;//模拟前21字节身份数据
		{
			zkSwitch.open();
			createZKClient();
			cli.executeLine("rmr " + BASE_PATH);//清空测试目录
			cli.executeLine("create " + BASE_PATH + " a");
			zkClient.waitToConnected(ZK_SERVER_START_TIMEOUT, TimeUnit.MILLISECONDS);
			cli.executeLine("create " + testPath + " " + createdData);
		}

		byte[] data = zkClient.getData(testPath, null, null);
		Assert.assertEquals(realData, new String(data));
	}
}
