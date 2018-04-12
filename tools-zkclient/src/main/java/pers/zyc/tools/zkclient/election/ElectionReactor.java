package pers.zyc.tools.zkclient.election;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.event.MulticastExceptionHandler;
import pers.zyc.tools.event.Multicaster;
import pers.zyc.tools.zkclient.BaseReactor;
import pers.zyc.tools.zkclient.LogMulticastExceptionHandler;
import pers.zyc.tools.zkclient.ZKClient;
import pers.zyc.tools.zkclient.listener.RecreateListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 选主实现
 *
 * @author zhangyancheng
 */
public class ElectionReactor extends BaseReactor implements RecreateListener, LeaderElection {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElectionReactor.class);

	/**
	 * 异常处理器, 所有事件发布异常都将记录日志
	 */
	private static final MulticastExceptionHandler EXCEPTION_HANDLER = new LogMulticastExceptionHandler(LOGGER);

	/**
	 * 选主节点名
	 */
	private String member;

	/**
	 * 主节点名
	 */
	private String leader;

	/**
	 * 选主信息
	 */
	private final ElectInfo electInfo;

	/**
	 * 选主事件发布器
	 */
	private Multicaster<EventListener<LeaderEvent>> multicaster = new Multicaster<EventListener<LeaderEvent>>() {};

	public ElectionReactor(ZKClient zkClient, ElectInfo electInfo) {
		super(zkClient);
		this.electInfo = electInfo;

		multicaster.setExceptionHandler(EXCEPTION_HANDLER);
	}

	@Override
	public void addListener(EventListener<LeaderEvent> listener) {
		multicaster.addListener(listener);
	}

	@Override
	public void removeListener(EventListener<LeaderEvent> listener) {
		multicaster.removeListener(listener);
	}

	@Override
	public void doStop() {
		super.doStop();
		//关闭election时需要放开leader
		releaseLeader(true);
	}

	@Override
	public void onDisconnected(boolean sessionClosed) {
		if (sessionClosed) {
			//会话超时需要放开leader
			releaseLeader(true);
		}
	}

	@Override
	public void onRecreateSuccessful(String path, String actualPath) {
		serviceLock.lock();
		try {
			member = actualPath;
			LOGGER.info("{} join election", member, path);
		} finally {
			serviceLock.unlock();
		}
	}

	@Override
	public void onRecreateFailed(String path, Exception exception) {
		LOGGER.error("Ephemeral node recreate failed, leader path: " + path, exception);
	}

	private void publish(LeaderEvent event) {
		multicaster.listeners.onEvent(event);
	}

	@Override
	protected void react(WatchedEvent event) {
		LeaderEvent leaderEvent = null;
		serviceLock.lock();
		try {
			if (!isRunning()) {
				return;
			}

			String electPath = electInfo.getElectPath();

			/*
			 * 初始以及成功放开主角色后member为null
			 *
			 * 创建member需保证electPath存在
			 */
			if (member == null) {
				String node = electPath + "/" + electInfo.getElectMode().prefix();

				//创建临时节点并加入自动重建
				member = zkClient.createEphemeral(node, electInfo.getMemberNodeData(), true, this);
				LOGGER.info("{} join election {}", member, electPath);
			}

			List<String> memberList = zkClient.getChildren(electPath, this);
			if (!memberList.contains(member)) {
				throw new IllegalStateException(member + " not in children list!");
			}

			if (isLeader()) {
				//当前已经是主节点则忽略所有子节点变更
				return;
			}

			String leastSeqNode = getLeastSeqNode(memberList);

			if (isObserver(leastSeqNode)) {
				if (leader != null) {
					leader = null;
					leaderEvent = LeaderEvent.CHANGED;
				}
				//只剩下observer节点
				LOGGER.warn("All member quit, now no leader online!");
			} else {
				//由最小序列号节点take leader, 如果是当前节点则发布TAKE事件
				leader = leastSeqNode;
				leaderEvent = isLeader() ? LeaderEvent.TAKE : LeaderEvent.CHANGED;
			}
		} catch (Exception e) {
			LOGGER.error("Leader react error, " + event, e);
		} finally {
			serviceLock.unlock();
		}

		if (leaderEvent != null) {
			//在锁外发布事件
			publish(leaderEvent);
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
	public boolean isLeader() {
		serviceLock.lock();
		try {
			return member != null && member.equals(leader);
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
			if (isLeader()) {
				boolean connected = zkClient.isConnected();
				if (force || connected) {
					if (connected) {
						zkClient.delete(electInfo.getElectPath() + "/" + member);
					}
					leader = member = null;
					released = true;
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (KeeperException e) {
			LOGGER.error("Release leader failed, node[{}], error: {}", member, e.getMessage());
		} finally {
			serviceLock.unlock();
		}

		if (released) {
			//在锁外发布事件
			publish(LeaderEvent.LOST);
		}
		return released;
	}

	@Override
	public ElectInfo getElectInfo() {
		return electInfo;
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
		return ElectMode.match(node) == ElectMode.OBSERVER;
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
				return Integer.parseInt(m1.substring(ElectMode.MEMBER.prefix().length())) -
						Integer.parseInt(m2.substring(ElectMode.MEMBER.prefix().length()));
			}
		}
	};
}
