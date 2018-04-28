package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.common.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.event.MulticastExceptionHandler;
import pers.zyc.tools.event.Multicaster;

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

	private static final WatchedEvent RE_ELECT_EVENT = CONNECTED_EVENT;

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
	 *选举路径
	 */
	private String electionPath;

	/**
	 * 选举人
	 */
	private Elector elector;

	private boolean releasing;

	private Condition quitCondition = serviceLock.newCondition();

	private volatile long disconnectedTime;

	/**
	 * 选主事件发布器
	 */
	private Multicaster<EventListener<ElectionEvent>> multicaster = new Multicaster<EventListener<ElectionEvent>>() {};

	ElectionReactor(ZKClient zkClient) {
		super(zkClient);

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
	protected void doStop() {
		while (member != null && !deleteSelf()) {
			long sessionTimeoutRemain = zkClient.getZooKeeper().getSessionTimeout() -
					(System.currentTimeMillis() - disconnectedTime);
			//quitCondition.await(sessionTimeoutRemain, TimeUnit.MILLISECONDS);
		}
		super.doStop();
	}

	@Override
	public void onDisconnected(boolean sessionClosed) {
		if (sessionClosed) {
			serviceLock.lock();
			try {
				if (releasing) {
					released();
				}
			} finally {
				serviceLock.unlock();
			}
		} else {
			disconnectedTime = System.currentTimeMillis();
		}
	}

	@Override
	public void onConnected(boolean newSession) {
		serviceLock.lock();
		try {
			if (releasing && deleteSelf()) {
				released();
			}
			super.onConnected(newSession);
		} finally {
			serviceLock.unlock();
		}
	}

	private void released() {
		member = null;
		releasing = false;

		if (isLeader) {
			isLeader = false;
			publish(ElectionEvent.LOST);
		}
	}

	private void publish(ElectionEvent event) {
		multicaster.listeners.onEvent(event);
	}

	@Override
	protected void react(WatchedEvent event) {
		/*
		 * 选举节点被删除, 无法创建子节点也无法获取子节点列表, 继而无法选举,
		 * 甚至连被删除事件本身都不是一定能被监听的到, 所以这里的提前终止(if分支里的return)
		 * 并不总能阻止报错, 也就没有了实际的防范意义.
		 *
		 * 总之,需要"外部"保证electionPath不被删除 且能读写子节点
		 */
		if (event.getType() == Event.EventType.NodeDeleted) {
			LOGGER.error("Election path deleted!");
			return;
		}

		ElectionEvent electionEvent = null;
		serviceLock.lock();
		try {
			if (!zkClient.isConnected()) {
				return;
			}

			List<String> memberList = null;

			//确定member是否仍然在选举列表
			if (member != null) {
				memberList = zkClient.getChildren(electionPath, this);

				if (!memberList.contains(member)) {
					member = null;

					if (isLeader) {
						isLeader = false;
						leader = null;
						publish(ElectionEvent.LOST);
					}
				} else if (isLeader) {
					//当前已经是主则忽略所有子节点变更
					return;
				}
			}

			/*
			 * 初始以及放开主角色后创建选举节点(不处理electPath不存在的错误情况)
			 */
			if (member == null) {
				String node = electionPath + "/" + elector.getElectionMode().prefix();

				member = zkClient.createEphemeral(node, elector.getMemberData(), true);
				LOGGER.info("{} join election {}", member, electionPath);

				memberList = zkClient.getChildren(electionPath, this);
			}

			String leastSeqNode = getLeastSeqNode(memberList);

			if (isObserver(leastSeqNode)) {
				//只剩下observer节点
				LOGGER.warn("All member is observer, no leader will be elected!");
				return;
			}

			//由最小序列号节点take leader, 如果是当前节点则发布TAKE事件
			leader = leastSeqNode;
			isLeader = member.equals(leader);
			electionEvent = isLeader ? ElectionEvent.TAKE : ElectionEvent.LEADER_CHANGED;
		} catch (Exception e) {
			LOGGER.error("Election react error, " + event, e);
		} finally {
			serviceLock.unlock();
		}

		if (electionEvent != null) {
			//在锁外发布事件
			publish(electionEvent);
		}
	}

	@Override
	public void elect(String electionPath, Elector elector) {
		Objects.requireNonNull(elector);
		PathUtils.validatePath(electionPath);
		serviceLock.lock();
		try {
			if (isRunning()) {
				//elect只能启动一次或者只能发生在quit之后
				return;
			}

			this.elector = elector;
			this.electionPath = electionPath;

			start();
		} finally {
			serviceLock.unlock();
		}
	}

	@Override
	public void quit() {
		stop();
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
	public void releaseLeader() {
		serviceLock.lock();
		try {
			if (isLeader && leader != null) {
				leader = null;

				if (release()) {
					isLeader = false;
					enqueueEvent(RE_ELECT_EVENT);
				}
			}
		} finally {
			serviceLock.unlock();
		}
	}

	private boolean release() {
		if (member == null || releasing) {
			return false;
		}

		releasing = !deleteSelf();

		return releasing;
	}

	/**
	 * 只在releaseLeader时才会调用删除自身节点
	 *
	 * <p>
	 *     如果删除异常表示删除失败, 这里认为异常即连接异常,
	 *     不考虑节点或者父节点已被删除的情况
	 *
	 * @return 是否成功删除
	 */
	private boolean deleteSelf() {
		try {
			if (zkClient.isConnected()) {
				zkClient.delete(electionPath + "/" + member);
			}
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
