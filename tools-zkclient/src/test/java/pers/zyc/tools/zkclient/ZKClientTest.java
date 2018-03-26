package pers.zyc.tools.zkclient;

import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.utils.TimeMillis;
import pers.zyc.tools.zkclient.listener.ConnectionListener;
import pers.zyc.tools.zkclient.listener.ExistsEventListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhangyancheng
 */
public class ZKClientTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZKClientTest.class);
	private ZKSwitch zkSwitch;
	private ZKClient zkClient;

	@Before
	public void setUp() {
		zkSwitch = new ZKSwitch("E:/Tools/zookeeper-3.4.10");
	}

	@After
	public void tearDown() {
		zkSwitch.close();
	}

	private void createZKClient(ClientConfig config) {
		zkClient = new ZKClient(config);
	}

	@Test
	public void case_syncStart() {
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setSyncStart(true);
		createZKClient(clientConfig);

		final int sleep = 8000;
		new Thread() {
			@Override
			public void run() {
				try {
					sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				LOGGER.info("Opening ZooKeeper server");
				zkSwitch.open();
			}
		}.start();
		long beforeTime = TimeMillis.get();
		zkClient.start();
		long endTime = TimeMillis.get();

		Assert.assertTrue(zkClient.isConnected());
		//异步进程启动需要时间, 使用1000ms容错
		Assert.assertTrue((endTime - beforeTime - sleep) < 1000);
	}

	@Test
	public void case_AddConnectionListener() throws InterruptedException {
		ClientConfig clientConfig = new ClientConfig();
		createZKClient(clientConfig);
		zkClient.start();

		final AtomicInteger events = new AtomicInteger();

		zkClient.addListener(new ConnectionListener() {
			@Override
			public void onConnected(boolean newSession) {
				events.getAndIncrement();
			}

			@Override
			public void onDisconnected(boolean sessionClosed) {
				events.getAndIncrement();
			}
		});

		zkSwitch.open();
		zkSwitch.close();
		zkSwitch.open();
		zkSwitch.close();

		Thread.sleep(clientConfig.getSessionTimeout());
		Assert.assertTrue(events.get() == 5);
	}

	/**
	 * 测试存在监听器
	 *
	 *
	 */
	@Test
	public void case_existsListener1() throws InterruptedException {
		zkSwitch.open();
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setSyncStart(true);
		createZKClient(clientConfig);
		zkClient.start();


		final CountDownLatch cdl = new CountDownLatch(4);

		zkClient.addListener("/zk-client/exists", new ExistsEventListener() {
			@Override
			public void onNodeCreated(String path, Stat nodeStat) {
				LOGGER.info("{} created {}", path, nodeStat);
				cdl.countDown();
			}

			@Override
			public void onNodeDeleted(String path) {
				LOGGER.info("{} deleted", path);
				cdl.countDown();
			}
		});



		cdl.await();
	}
}
