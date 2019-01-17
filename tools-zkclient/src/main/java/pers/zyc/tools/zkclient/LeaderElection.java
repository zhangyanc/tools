package pers.zyc.tools.zkclient;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.utils.event.EventListener;
import pers.zyc.tools.utils.event.MulticastExceptionHandler;
import pers.zyc.tools.utils.event.Multicaster;
import pers.zyc.tools.utils.lifecycle.ServiceState;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Condition;

/**
 * 选主实现
 *
 * @author zhangyancheng
 */
class LeaderElection extends Reactor implements Election {

	private static final Logger LOGGER = LoggerFactory.getLogger(LeaderElection.class);

	/**
	 * 异常处理器, 所有事件发布异常都将记录日志
	 */
	private static final MulticastExceptionHandler EXCEPTION_HANDLER = new LogMulticastExceptionHandler(LOGGER);

	/**
	 * 会话关闭标志事件
	 */
	private static final WatchedEvent SESSION_CLOSED_EVENT = new WatchedEvent(null, null, null);

	/**
	 * 节点数据
	 */
	private final byte[] memberData;

	/**
	 * 选举模式
	 */
	private final Mode electorMode;

	/**
	 * 退出条件
	 */
	private final Condition quitCondition = serviceLock.newCondition();

	/**
	 * 选主事件发布器
	 */
	private final Multicaster<EventListener<ElectionEvent>> multicaster = new Multicaster<EventListener<ElectionEvent>>() {
		{
			setExceptionHandler(EXCEPTION_HANDLER);
		}
	};

	/**
	 * 选主节点名
	 */
	private String member;

	/**
	 * 主节点名
	 */
	private String leader;

	LeaderElection(String electionPath, ZKClient zkClient,
				   Mode electorMode, byte[] memberData) {

		super(electionPath, zkClient);
		this.memberData = memberData;
		this.electorMode = electorMode;

		start();
	}

	@Override
	public void addListener(EventListener<ElectionEvent> listener) {
		multicaster.addListener(listener);
	}

	@Override
	public void removeListener(EventListener<ElectionEvent> listener) {
		multicaster.removeListener(listener);
	}

	@Override
	protected void doStop() {
		while (member != null) {
			deleteSelf();
			LOGGER.info("Quiting election {}, wait last react", path);
			quitCondition.awaitUninterruptibly();
		}
		super.doStop();
	}

	@Override
	public void onConnected(boolean newSession) {
		if (newSession) {
			enqueueEvent(CONNECTED_EVENT);
		} else if (isQuitting()) {
			serviceLock.lock();
			try {
				if (isQuitting()) {
					quitCondition.signal();
				}
			} finally {
				serviceLock.unlock();
			}
		}
	}

	@Override
	public void onDisconnected(boolean sessionClosed) {
		if (sessionClosed) {
			enqueueEvent(SESSION_CLOSED_EVENT);
		}
	}

	@Override
	public byte[] memberData() {
		return memberData;
	}

	@Override
	public Mode mode() {
		return electorMode;
	}

	@Override
	protected void react(WatchedEvent event) {
		ElectionEvent electionEvent = null;
		serviceLock.lock();
		try {
			boolean isQuitting = isQuitting();
			if (isQuitting || event == SESSION_CLOSED_EVENT) {
				LOGGER.info("{} quit election {}", member, path);
				//member已经不存在
				if (isLeader()) {
					electionEvent = new ElectionEvent(this, EventType.LEADER_LOST);
				}
				member = leader = null;
				if (isQuitting) {
					quitCondition.signal();
				}
			} else if (zkClient.isConnected()) {
				if (member == null) {
					member = zkClient.create(path + "/" + electorMode.prefix(),
							memberData, CreateMode.EPHEMERAL_SEQUENTIAL).substring(path.length() + 1);
					LOGGER.info("{} join election {}", member, path);
				}

				electionEvent = elect();
			}
		} catch (Exception e) {
			LOGGER.error("Election watch error, " + event, e);
		} finally {
			serviceLock.unlock();
		}

		if (electionEvent != null) {
			//在锁外发布事件
			multicaster.listeners.onEvent(electionEvent);
		}
	}

	private ElectionEvent elect() throws KeeperException, InterruptedException {
		List<String> children = zkClient.getChildren(path, this);

		if (!children.contains(member)) {
			throw new IllegalStateException(member + " not in children list!");
		}
		if (isLeader()) {
			//主节点不用处理变更(其他member的新增、删除事件)
			return null;
		}

		String leastSeqNode = getLeastSeqNode(children);

		if (isObserver(leastSeqNode)) {
			LOGGER.warn("All member is observer, no leader elected!");

			if (leader != null) {
				leader = null;
				return new ElectionEvent(this, EventType.LEADER_CHANGED);
			}
			return null;
		} else if (leastSeqNode.equals(leader)) {
			//主节点未变更
			return null;
		}

		leader = leastSeqNode;
		return new ElectionEvent(this, isLeader() ? EventType.LEADER_TOOK : EventType.LEADER_CHANGED);
	}

	@Override
	public String electionPath() {
		return getPath();
	}

	@Override
	public String member() {
		serviceLock.lock();
		try {
			return member;
		} finally {
			serviceLock.unlock();
		}
	}

	@Override
	public String leader() {
		serviceLock.lock();
		try {
			return leader;
		} finally {
			serviceLock.unlock();
		}
	}

	@Override
	public boolean isLeader() {
		serviceLock.lock();
		try {
			return member != null && member.equals(leader);
		} finally {
			serviceLock.unlock();
		}
	}

	@Override
	public void reelect() {
		serviceLock.lock();
		try {
			if (!isRunning()) {
				//elect过程中才可以reelect
				return;
			}
			stop();
			start();
		} finally {
			serviceLock.unlock();
		}
	}

	@Override
	public void quit() {
		stop();
	}

	private boolean isQuitting() {
		return checkState(ServiceState.STOPPING);
	}

	/**
	 * 删除当前节点
	 */
	private void deleteSelf() {
		try {
			if (zkClient.isConnected()) {
				zkClient.delete(path + "/" + member);
			}
		} catch (KeeperException.NoNodeException noNodeException) {
			LOGGER.warn("Member[{}] already deleted", member);
		} catch (Exception e) {
			LOGGER.error("Delete member[{}] error: {}", member, e.getMessage());
		}
	}

	/**
	 * 返回子节点列表中序列号最小的节点
	 *
	 * @param children 子节点列表
	 */
	private static String getLeastSeqNode(List<String> children) {
		if (children.size() > 1) {
			Collections.sort(children, MEMBER_COMPARATOR);
		}
		return children.get(0);
	}

	/**
	 * 检查节点名是否observer模式
	 *
	 * @param node 节点名
	 */
	private static boolean isObserver(String node) {
		return Mode.match(node) == Mode.OBSERVER;
	}

	/**
	 * 节点排序器
	 *
	 * <p>
	 *     将所有member节点按序列号由小到大排列, 且全部排在observer前面
	 *
	 */
	private static final Comparator<String> MEMBER_COMPARATOR = new Comparator<String>() {

		@Override
		public int compare(String m1, String m2) {
			if (isObserver(m1)) {
				return 1;
			} else if (isObserver(m2)) {
				return -1;
			} else {
				//截取末尾10位序号数组判断大小
				return Integer.parseInt(m1.substring(m1.length() - 10)) -
						Integer.parseInt(m2.substring(m2.length() - 10));
			}
		}
	};
}
