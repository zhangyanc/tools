package pers.zyc.tools.zkclient;

import pers.zyc.tools.utils.event.EventSource;

/**
 * @author zhangyancheng
 */
public interface Elector extends EventSource<ElectionEvent> {

	/**
	 * @return member节点数据
	 */
	byte[] memberData();

	/**
	 * @return 选主模式
	 */
	Mode mode();

	/**
	 * @return 选举路径
	 */
	String electionPath();

	/**
	 * @return 当前member节点名
	 */
	String member();

	/**
	 * @return leader节点名
	 */
	String leader();

	/**
	 * @return 当前是否已选为leader
	 */
	boolean isLeader();

	enum Mode {
		/**
		 * 候选者
		 */
		FOLLOWER("follower-"),

		/**
		 * 观察者(不参与选主)
		 */
		OBSERVER("observer-");

		/**
		 * 节点前缀
		 */
		private String prefix;

		Mode(String prefix) {
			this.prefix = prefix;
		}

		public String prefix() {
			return prefix;
		}

		public static Mode match(String node) {
			for (Mode mode : values()) {
				if (node.startsWith(mode.prefix)) {
					return mode;
				}
			}
			throw new IllegalArgumentException("UnMatch node " + node);
		}
	}
}
