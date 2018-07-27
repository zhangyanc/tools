package pers.zyc.tools.zkclient;

import org.apache.zookeeper.*;
import org.junit.Assert;
import org.junit.Test;
import pers.zyc.tools.utils.event.EventListener;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhangyancheng
 */
public class ElectionTest extends BaseClientTest {

	private static class TestMember {
		String member;
		ZooKeeper session;

		TestMember(String member, ZooKeeper session) {
			this.member = member;
			this.session = session;
		}

		void exit() throws Exception {
			makeZooKeeperSessionExpire(session);
		}
	}

	private static final String ELECTION_PATH = "/election";

	private void prepare() throws InterruptedException, IOException, KeeperException {
		ClientConfig config = new ClientConfig();
		config.setConnectStr(CONNECT_STRING);

		createZKClient(config);

		zkSwitch.open();

		cli.executeLine("rmr " + ELECTION_PATH);
		cli.executeLine("create " + ELECTION_PATH + " a");
	}

	private TestMember addMember(Elector.Mode electorMode) throws Exception {
		ZooKeeper session = new ZooKeeper(CONNECT_STRING, 30000, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
			}
		});

		String member = session.create(ELECTION_PATH + "/" + electorMode.prefix(), new byte[0],
				ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

		return new TestMember(member.substring(member.lastIndexOf("/") + 1), session);
	}

	@Test
	public void case_Elect_FirstFollowerTakeLeader() throws Exception {
		prepare();

		addMember(Elector.Mode.OBSERVER);

		Election election = zkClient.createElection(ELECTION_PATH);

		Assert.assertEquals(election.electionPath(), ELECTION_PATH);
		Assert.assertTrue(election.mode() == Elector.Mode.FOLLOWER);
		Assert.assertArrayEquals(election.memberData(), new byte[0]);

		final CountDownLatch cdl = new CountDownLatch(1);
		election.addListener(new EventListener<ElectionEvent>() {
			@Override
			public void onEvent(ElectionEvent event) {
				logger.info("ElectionEvent " + event.eventType);
				if (event.eventType == Election.EventType.LEADER_TOOK) {
					cdl.countDown();
				}
			}
		});

		//添加监听器前已经启动election, 有可能错过TOOK事件(如果添加后未选为主, 则必需收到事件)
		if (!election.isLeader() && !cdl.await(2000, TimeUnit.MILLISECONDS)) {
			Assert.fail("Wait event timeout!");
		}

		Assert.assertTrue(election.isLeader());
		Assert.assertTrue(election.leader().equals(election.member()));
	}

	@Test
	public void case_Elect_SecondFollowerTakeLeaderAfterFirstFollowerDeleted() throws Exception {
		prepare();

		TestMember firstFollower = addMember(Elector.Mode.FOLLOWER);

		Election election = zkClient.createElection(ELECTION_PATH, Elector.Mode.FOLLOWER, new byte[0]);

		Thread.sleep(2000);
		Assert.assertTrue(election.leader().equals(firstFollower.member));
		Assert.assertFalse(election.isLeader());


		final CountDownLatch cdl = new CountDownLatch(1);
		election.addListener(new EventListener<ElectionEvent>() {
			@Override
			public void onEvent(ElectionEvent event) {
				logger.info("ElectionEvent " + event.eventType);
				if (event.eventType == Election.EventType.LEADER_TOOK) {
					cdl.countDown();
				}
			}
		});

		firstFollower.exit();//触发选举

		if (!cdl.await(2000, TimeUnit.MILLISECONDS)) {
			Assert.fail("Wait event timeout!");
		}
		Assert.assertTrue(election.isLeader());
		Assert.assertTrue(election.leader().equals(election.member()));
	}

	@Test
	public void case_Elect_Reelect() throws Exception {
		prepare();

		addMember(Elector.Mode.OBSERVER);

		final Election election = zkClient.createElection(ELECTION_PATH, Elector.Mode.FOLLOWER, new byte[0]);

		final Semaphore semaphore = new Semaphore(0);
		final AtomicReference<Election.EventType> electionEvent = new AtomicReference<>();
		election.addListener(new EventListener<ElectionEvent>() {

			@Override
			public void onEvent(ElectionEvent event) {
				logger.info("ElectionEvent " + event.eventType);
				electionEvent.set(event.eventType);
				semaphore.release();
			}
		});

		{//第一个follower, 被选为leader
			//添加监听器前已经启动election, 有可能错过TOOK事件(如果添加后未选为主, 则必需收到事件)
			if (!election.isLeader() && !semaphore.tryAcquire(2000, TimeUnit.MILLISECONDS)) {
				Assert.fail("Wait event timeout!");
			}
			Assert.assertTrue(election.isLeader());
			Assert.assertTrue(electionEvent.get() == null || electionEvent.get() == Election.EventType.LEADER_TOOK);
		}

		TestMember secondFollower = addMember(Elector.Mode.FOLLOWER);
		TestMember thirdFollower = addMember(Elector.Mode.FOLLOWER);

		{//重新选, 发布LOST事件, 将作为最后一个follower重新加入
			election.reelect();
			if (!semaphore.tryAcquire(2000, TimeUnit.MILLISECONDS)) {
				Assert.fail("Wait event timeout!");
			}
			Assert.assertTrue(electionEvent.get() == Election.EventType.LEADER_LOST);
		}

		{//重选后, 第二个follower被选为leader
			if (!semaphore.tryAcquire(2000, TimeUnit.MILLISECONDS)) {
				Assert.fail("Wait event timeout!");
			}
			Assert.assertTrue(electionEvent.get() == Election.EventType.LEADER_CHANGED);
			Assert.assertTrue(election.leader().equals(secondFollower.member));
		}

		{//第二个follower(leader)退出, 第三个follower被选为leader
			secondFollower.exit();
			if (!semaphore.tryAcquire(2000, TimeUnit.MILLISECONDS)) {
				Assert.fail("Wait event timeout!");
			}
			Assert.assertTrue(electionEvent.get() == Election.EventType.LEADER_CHANGED);
			Assert.assertTrue(election.leader().equals(thirdFollower.member));
		}

		{//第三个follower(leader)退出, elector将被重新选为leader
			thirdFollower.exit();
			if (!semaphore.tryAcquire(2000, TimeUnit.MILLISECONDS)) {
				Assert.fail("Wait event timeout!");
			}
			Assert.assertTrue(electionEvent.get() == Election.EventType.LEADER_TOOK);
			Assert.assertTrue(election.isLeader());
		}

		{//退出选举, 作为leader退出发布LOST事件
			election.quit();
			if (!semaphore.tryAcquire(2000, TimeUnit.MILLISECONDS)) {
				Assert.fail("Wait event timeout!");
			}
			Assert.assertTrue(electionEvent.get() == Election.EventType.LEADER_LOST);
			Assert.assertFalse(election.isLeader());
		}

		Assert.assertNull(election.leader());
		Assert.assertNull(election.member());
	}
}
