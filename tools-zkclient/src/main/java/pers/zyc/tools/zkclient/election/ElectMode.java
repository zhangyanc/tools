package pers.zyc.tools.zkclient.election;

/**
 * @author zhangyancheng
 */
public enum ElectMode {
	MEMBER("member-"), OBSERVER("observer-");

	private String prefix;

	ElectMode(String prefix) {
		this.prefix = prefix;
	}

	public String prefix() {
		return prefix;
	}

	public static ElectMode match(String node) {
		for (ElectMode mode : values()) {
			if (node.startsWith(mode.prefix)) {
				return mode;
			}
		}
		throw new IllegalArgumentException("UnMatch node " + node);
	}
}
