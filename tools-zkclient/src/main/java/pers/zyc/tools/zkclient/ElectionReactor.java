package pers.zyc.tools.zkclient;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.event.MulticastExceptionHandler;
import pers.zyc.tools.event.Multicaster;
import pers.zyc.tools.lifecycle.ServiceState;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Condition;

/**
 * 选主实现
 *
 * @author zhangyancheng
 */
class ElectionReactor extends BaseReactor implements LeaderElection {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElectionReactor.class);

	/**
	 * 异常处理器, 所有事件发布异常都将记录日志
	 */
	private static final MulticastExceptionHandler EXCEPTION_HANDLER = new LogMulticastExceptionHandler(LOGGER);

	private static final WatchedEvent QUITED_EVENT = new WatchedEvent(null, null, null);

	/**
	 * 选主节点名
	 */
	private String member;

	/**
	 * 主节点名
	 */
	private String leader;

	/**
	 * 当前是否已选为主
	 */
	private boolean isLeader;

	/**
	 * 选举人
	 */
	private Elector elector;

	private final Condition quitCondition = serviceLock.newCondition();

	/**
	 * 选主事件发布器
	 */
	private Multicaster<EventListener<ElectionEvent>> multicaster = new Multicaster<EventListener<ElectionEvent>>() {};

	ElectionReactor(String electionPath, ZKClient zkClient) {
		super(electionPath, zkClient);

		multicaster.setExceptionHandler(EXCEPTION_HANDLER);
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
	protected void beforeStart() throws Exception {
		Objects.requireNonNull(elector);
	}

	@Override
	protected void doStop() {
		while (member != null && !deleteSelf()) {
			quitCondition.awaitUninterruptibly();
		}
		enqueueEvent(QUITED_EVENT);
		super.doStop();
	}

	@Override
	public void onDisconnected(boolean sessionClosed) {
		serviceLock.lock();
		try {
			if (sessionClosed && isQuitting()) {
				member = leader = null;
				quitCondition.notify();
			}
		} finally {
			serviceLock.unlock();
		}
	}

	@Override
	public void onConnected(boolean newSession) {
		serviceLock.lock();
		try {
			if (isQuitting()) {
				quitCondition.notify();
			} else {
				super.onConnected(newSession);
			}
		} finally {
			serviceLock.unlock();
		}
	}

	@Override
	protected void react(WatchedEvent event) {
		ElectionEvent electionEvent = null;
		serviceLock.lock();
		try {
			if (event == QUITED_EVENT && isLeader) {
				isLeader = false;
				electionEvent = ElectionEvent.LOST;
			} else {
				if (member == null) {
					String node = path + "/" + elector.getElectionMode().prefix();

					member = zkClient.createEphemeral(node, elector.getMemberData(), true);
					LOGGER.info("{} join election {}", member, path);
				}

				electionEvent = elect();
			}
		} catch (Exception e) {
			LOGGER.error("Election react error, " + event, e);
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

		//发生了
		if (!children.contains(member)) {
			member = null;
			return isLeader ? ElectionEvent.LOST : ElectionEvent.LEADER_CHANGED;
		}

		String leastSeqNode = getLeastSeqNode(children);

		if (isObserver(leastSeqNode)) {
			//全部是observer节点
			LOGGER.warn("All member is observer, no leader elected!");
			return null;
		}

		String lastLeader = leader;
		//由最小序列号节点获取主角色, 如果是当前节点则发布TAKE事件
		leader = leastSeqNode;
		isLeader = member.equals(leader);
		boolean leaderChanged = !leader.equals(lastLeader);

		return isLeader ? ElectionEvent.TAKE : leaderChanged ? ElectionEvent.LEADER_CHANGED : null;
	}

	@Override
	public void elect(Elector elector) {
		serviceLock.lock();
		try {
			if (isRunning()) {
				//elect只能启动一次或者只能发生在quit之后
				return;
			}

			this.elector = elector;

			start();
		} finally {
			serviceLock.unlock();
		}
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
	 * @return 是否正确删除了当前节点
	 */
	private boolean deleteSelf() {
		try {
			if (zkClient.isConnected()) {
				zkClient.delete(path + "/" + member);
				return true;
			}
		} catch (KeeperException.NoNodeException noNodeException) {
			LOGGER.warn("Member[{}] already deleted", member);
			return true;
		} catch (Exception e) {
			LOGGER.error("Delete member[{}] error: {}", member, e.getMessage());
		}
		return false;
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
		return ElectionMode.match(node) == ElectionMode.OBSERVER;
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
				return Integer.parseInt(m1.substring(ElectionMode.MEMBER.prefix().length())) -
						Integer.parseInt(m2.substring(ElectionMode.MEMBER.prefix().length()));
			}
		}
	};
}
