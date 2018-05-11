package pers.zyc.tools.zkclient;

/**
 * @author zhangyancheng
 */
public enum ElectorMode {
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

	ElectorMode(String prefix) {
		this.prefix = prefix;
	}

	public String prefix() {
		return prefix;
	}

	public static ElectorMode match(String node) {
		for (ElectorMode mode : values()) {
			if (node.startsWith(mode.prefix)) {
				return mode;
			}
		}
		throw new IllegalArgumentException("UnMatch node " + node);
	}
}
