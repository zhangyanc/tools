package pers.zyc.tools.zkclient;

/**
 * @author zhangyancheng
 */
public enum ElectionMode {
	/**
	 * 候选者
	 */
	MEMBER("member-"),

	/**
	 * 观察者(不参与选主)
	 */
	OBSERVER("observer-");

	/**
	 * 节点前缀
	 */
	private String prefix;

	ElectionMode(String prefix) {
		this.prefix = prefix;
	}

	public String prefix() {
		return prefix;
	}

	public static ElectionMode match(String node) {
		for (ElectionMode mode : values()) {
			if (node.startsWith(mode.prefix)) {
				return mode;
			}
		}
		throw new IllegalArgumentException("UnMatch node " + node);
	}
}
