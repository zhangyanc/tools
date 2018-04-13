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

	private static final WatchedEvent LEADER_RELEASED_EVENT = CONNECTED_EVENT;

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

	private String electionPath;

	/**
	 * 选主信息
	 */
	private Elector elector;

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
		super.doStop();
		releaseLeader(true);
	}

	@Override
	public void onDisconnected(boolean sessionClosed) {
		if (sessionClosed) {
			releaseLeader(true);
		}
	}

	@Override
	public void onConnected(boolean newSession) {
		if (isLeader && (newSession || (leader == null && deleteSelf()))) {
			//新会话或者release没能删除节点重连成功删除后重置isLeaser标志
			isLeader = false;
		}
		super.onConnected(newSession);
	}

	private void publish(ElectionEvent event) {
		multicaster.listeners.onEvent(event);
	}

	@Override
	protected void react(WatchedEvent event) {
		ElectionEvent electionEvent = null;
		serviceLock.lock();
		try {
			if (!isRunning()) {
				return;
			}

			/*
			 * 初始以及成功放开主角色后member为null
			 *
			 * 创建member需保证electPath存在
			 */
			if (member == null) {
				String node = electionPath + "/" + elector.getElectionMode().prefix();

				member = zkClient.createEphemeral(node, elector.getMemberData(), true);
				LOGGER.info("{} join election {}", member, electionPath);
			}

			List<String> memberList = zkClient.getChildren(electionPath, this);
			if (!memberList.contains(member)) {
				throw new IllegalStateException(member + " not in children list!");
			}

			if (isLeader) {
				//当前已经是主节点则忽略所有子节点变更
				return;
			}

			String leastSeqNode = getLeastSeqNode(memberList);

			if (isObserver(leastSeqNode)) {
				if (leader != null) {
					leader = null;
					electionEvent = ElectionEvent.LEADER_CHANGED;
				}
				//只剩下observer节点
				LOGGER.warn("All member quit, now no leader online!");
			} else {
				//由最小序列号节点take leader, 如果是当前节点则发布TAKE事件
				leader = leastSeqNode;
				isLeader = member.equals(leader);
				electionEvent = isLeader ? ElectionEvent.TAKE : ElectionEvent.LEADER_CHANGED;
			}
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
		if (isRunning()) {
			return;
		}

		this.elector = elector;
		PathUtils.validatePath(electionPath);
		this.electionPath = electionPath;

		start();
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
	public boolean releaseLeader() {
		return releaseLeader(false);
	}

	/**
	 * 放开主角色
	 *
	 * <p>
	 *     1. 当前是主节点时才放开主
	 *     2. 如果非强制release需要当前zookeeper必需连通且成功删除了节点
	 *
	 * @param force 是否强制, 会话超时或者关闭election时属强制release
	 */
	private boolean releaseLeader(boolean force) {
		boolean released = false;
		serviceLock.lock();
		try {
			if (isLeader) {
				boolean connected = zkClient.isConnected();
				if (force || connected) {
					if (connected && deleteSelf()) {
						//成功删除才重置isLeader标志, 否则将在重连成功时再删除
						isLeader = false;
					}
					leader = null;
					released = true;
				}
			}
		} finally {
			serviceLock.unlock();
		}

		if (released) {
			//在锁外发布事件
			publish(ElectionEvent.LOST);
			if (!force) {
				enqueueEvent(LEADER_RELEASED_EVENT);
			}
		}
		return released;
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
			zkClient.delete(electionPath + "/" + member);
			member = null;
			return true;
		} catch (Exception e) {
			LOGGER.error("Delete self error: {} for member: {}", e.getMessage(), member);
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
