package pers.zyc.tools.zkclient;

import org.apache.zookeeper.*;
import org.junit.Assert;
import org.junit.Test;
import pers.zyc.tools.event.EventListener;

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

	@Test
	public void case_UnElect() throws Exception {
		prepare();

		LeaderElection election = zkClient.getElection(ELECTION_PATH);

		Assert.assertNull(election.leader());
		Assert.assertNull(election.member());
	}

	private void prepare() throws InterruptedException, IOException, KeeperException {
		ClientConfig config = new ClientConfig();
		config.setSyncStart(true);
		config.setConnectStr(CONNECT_STRING);

		createZKClient(config);

		zkSwitch.open();
		zkClient.start();

		cli.executeLine("rmr " + ELECTION_PATH);
		cli.executeLine("create " + ELECTION_PATH + " a");
	}

	private TestMember addMember(ElectorMode electorMode) throws Exception {
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

		addMember(ElectorMode.OBSERVER);

		LeaderElection election = zkClient.getElection(ELECTION_PATH);
		Elector elector = new DefaultElector(new byte[0], ElectorMode.FOLLOWER);

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

		cdl.await(2000, TimeUnit.MILLISECONDS);
		Assert.assertTrue(election.leader().equals(election.member()));
	}

	@Test
	public void case_Elect_SecondFollowerTakeLeaderAfterFirstFollowerDeleted() throws Exception {
		prepare();

		TestMember firstFollower = addMember(ElectorMode.FOLLOWER);

		LeaderElection election = zkClient.getElection(ELECTION_PATH);
		Elector elector = new DefaultElector(new byte[0], ElectorMode.FOLLOWER);

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

		Assert.assertFalse(cdl.await(2000, TimeUnit.MILLISECONDS));
		Assert.assertTrue(election.leader().equals(firstFollower.member));

		firstFollower.exit();
		cdl.await(2000, TimeUnit.MILLISECONDS);
		Assert.assertTrue(election.leader().equals(election.member()));
	}

	@Test
	public void case_Elect_Reelect() throws Exception {
		prepare();

		addMember(ElectorMode.OBSERVER);

		LeaderElection election = zkClient.getElection(ELECTION_PATH);
		Elector elector = new DefaultElector(new byte[0], ElectorMode.FOLLOWER);

		final Semaphore semaphore = new Semaphore(0);
		final AtomicReference<ElectionEvent> electionEvent = new AtomicReference<>();
		election.addListener(new EventListener<ElectionEvent>() {

			@Override
			public void onEvent(ElectionEvent event) {
				logger.info("Leader " + event);
				electionEvent.set(event);
				semaphore.release();
			}
		});

		{//第一个follower, 被选为leader
			election.elect(elector);
			semaphore.tryAcquire(2000, TimeUnit.MILLISECONDS);
			Assert.assertTrue(electionEvent.get() == ElectionEvent.TAKE);
			Assert.assertTrue(election.leader().equals(election.member()));
		}

		TestMember secondFollower = addMember(ElectorMode.FOLLOWER);
		TestMember thirdFollower = addMember(ElectorMode.FOLLOWER);

		{//重新选, 发布LOST事件, 将作为最后一个follower加入
			election.reelect();
			semaphore.tryAcquire(2000, TimeUnit.MILLISECONDS);
			Assert.assertTrue(electionEvent.get() == ElectionEvent.LOST);
		}

		{//重选后, 第二个follower被选为leader
			semaphore.tryAcquire(2000, TimeUnit.MILLISECONDS);
			Assert.assertTrue(electionEvent.get() == ElectionEvent.LEADER_CHANGED);
			Assert.assertTrue(election.leader().equals(secondFollower.member));
		}

		{//第二个follower(leader)退出, 第三个follower被选为leader
			secondFollower.exit();
			semaphore.tryAcquire(2000, TimeUnit.MILLISECONDS);
			Assert.assertTrue(electionEvent.get() == ElectionEvent.LEADER_CHANGED);
			Assert.assertTrue(election.leader().equals(thirdFollower.member));
		}

		{//第三个follower(leader)退出, elector将被重新选为leader
			thirdFollower.exit();
			semaphore.tryAcquire(2000, TimeUnit.MILLISECONDS);
			Assert.assertTrue(electionEvent.get() == ElectionEvent.TAKE);
			Assert.assertTrue(election.leader().equals(election.member()));
		}

		{//退出选举, 作为leader退出发布LOST事件
			election.quit();
			semaphore.tryAcquire(2000, TimeUnit.MILLISECONDS);
			Assert.assertTrue(electionEvent.get() == ElectionEvent.LOST);
		}

		Assert.assertNull(election.leader());
		Assert.assertNull(election.member());
	}
}
