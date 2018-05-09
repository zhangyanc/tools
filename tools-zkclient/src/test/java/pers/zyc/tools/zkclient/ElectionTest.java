package pers.zyc.tools.zkclient;

import org.apache.zookeeper.KeeperException;
import org.junit.Assert;
import org.junit.Test;
import pers.zyc.tools.event.EventListener;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author zhangyancheng
 */
public class ElectionTest extends BaseClientTest {

	private static final String ELECTION_PATH = "/election";

	@Test
	public void case_UnElect() throws Exception {
		ClientConfig config = new ClientConfig();
		config.setSyncStart(true);
		config.setConnectStr(CONNECT_STRING);

		createZKClient(config);

		zkSwitch.open();
		zkClient.start();

		LeaderElection election = zkClient.getElection(ELECTION_PATH);

		Assert.assertNull(election.leader());
		Assert.assertNull(election.member());
	}

	private void prepare() throws InterruptedException, IOException, KeeperException {
		cli.executeLine("rmr " + ELECTION_PATH);
		cli.executeLine("create " + ELECTION_PATH + " e");
	}

	@Test
	public void case_Elect() throws Exception {
		ClientConfig config = new ClientConfig();
		config.setSyncStart(true);
		config.setConnectStr(CONNECT_STRING);

		createZKClient(config);

		zkSwitch.open();
		zkClient.start();

		//清理并创建选举路径
		prepare();

		LeaderElection election = zkClient.getElection(ELECTION_PATH);
		Elector elector = new DefaultElector(new byte[0], ElectionMode.MEMBER);

		final CountDownLatch cdl = new CountDownLatch(1);
		election.addListener(new EventListener<ElectionEvent>() {
			@Override
			public void onEvent(ElectionEvent event) {
				logger.info("Leader " + event);
				if (event == ElectionEvent.TAKE) {
					cdl.countDown();
				}
			}
		});

		election.elect(elector);
		cdl.await();

		Assert.assertTrue(election.leader().equals(election.member()));
	}
}
