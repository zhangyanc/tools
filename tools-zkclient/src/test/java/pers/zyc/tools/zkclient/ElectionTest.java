package pers.zyc.tools.zkclient;

import org.apache.zookeeper.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pers.zyc.tools.utils.event.EventListener;

import java.util.concurrent.*;
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

	@Before
	public void setUp() throws Exception {
		createZKClient(CONNECT_STRING, SESSION_TIMEOUT, 2, 3000);
		createSwitch();
		createCli();
		zkSwitch.open();

		zkClient.waitToConnected(ZK_SERVER_START_TIMEOUT, TimeUnit.MILLISECONDS);

		cli.executeLine("rmr " + ELECTION_PATH);
		cli.executeLine("create " + ELECTION_PATH + " a");
	}

	@Test
	public void case_Elect_FirstFollowerTakeLeader() throws Exception {
		addMember(Elector.Mode.OBSERVER);

		Election election = zkClient.createElection(ELECTION_PATH);

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

		//添加监听器前已经启动election, 有可能错过TOOK事件(如果添加前未选为主, 则必需收到TOOK事件)
		if (!election.isLeader() && !cdl.await(1000, TimeUnit.MILLISECONDS)) {
			Assert.fail("Wait event timeout!");
		}

		Assert.assertTrue(election.mode() == Elector.Mode.FOLLOWER);
		Assert.assertEquals(election.electionPath(), ELECTION_PATH);
		Assert.assertArrayEquals(election.memberData(), new byte[0]);

		Assert.assertTrue(election.isLeader());
		Assert.assertTrue(election.leader().equals(election.member()));
	}

	@Test
	public void case_Elect_SecondFollowerTakeLeaderAfterFirstFollowerDeleted() throws Exception {

		TestMember firstFollower = addMember(Elector.Mode.FOLLOWER);

		Election election = zkClient.createElection(ELECTION_PATH, Elector.Mode.FOLLOWER, new byte[0]);

		Thread.sleep(1000);
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

		if (!cdl.await(1000, TimeUnit.MILLISECONDS)) {
			Assert.fail("Wait event timeout!");
		}
		Assert.assertTrue(election.isLeader());
		Assert.assertTrue(election.leader().equals(election.member()));
	}

	@Test
	public void case_Elect_Reelect() throws Exception {
		addMember(Elector.Mode.OBSERVER);

		final Election election = zkClient.createElection(ELECTION_PATH, Elector.Mode.FOLLOWER, new byte[0]);

		final AtomicReference<Election.EventType> electionEvent = new AtomicReference<>();
		final Semaphore semaphore = new Semaphore(0);

		election.addListener(new EventListener<ElectionEvent>() {

			@Override
			public void onEvent(ElectionEvent event) {
				logger.info("ElectionEvent " + event.eventType);
				electionEvent.set(event.eventType);
				semaphore.release();
			}
		});

		{//消耗可能的TOOK信号
			semaphore.tryAcquire(1000, TimeUnit.MILLISECONDS);
			Assert.assertTrue(election.isLeader());
		}

		TestMember secondFollower = addMember(Elector.Mode.FOLLOWER);
		TestMember thirdFollower = addMember(Elector.Mode.FOLLOWER);

		{
			//重新选, 发布LOST事件, 将作为最后一个follower重新加入
			//重选后, 第二个follower被选为leader
			election.reelect();
			if (!semaphore.tryAcquire(2, 1000, TimeUnit.MILLISECONDS)) {
				Assert.fail("Wait event timeout!");
			}
			Assert.assertEquals(Election.EventType.LEADER_CHANGED, electionEvent.get());
			Assert.assertEquals(election.leader(), secondFollower.member);
		}

		{//第二个follower(leader)退出, 第三个follower被选为leader
			secondFollower.exit();
			if (!semaphore.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
				Assert.fail("Wait event timeout!");
			}
			Assert.assertEquals(Election.EventType.LEADER_CHANGED, electionEvent.get());
			Assert.assertEquals(election.leader(), thirdFollower.member);
		}

		{//第三个follower(leader)退出, elector将被重新选为leader
			thirdFollower.exit();
			if (!semaphore.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
				Assert.fail("Wait event timeout!");
			}
			Assert.assertEquals(Election.EventType.LEADER_TOOK, electionEvent.get());
			Assert.assertTrue(election.isLeader());
		}

		{//退出选举, 作为leader退出发布LOST事件
			election.quit();
			if (!semaphore.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
				Assert.fail("Wait event timeout!");
			}
			Assert.assertEquals(Election.EventType.LEADER_LOST, electionEvent.get());
			Assert.assertFalse(election.isLeader());
		}

		Assert.assertNull(election.leader());
		Assert.assertNull(election.member());
	}
}
