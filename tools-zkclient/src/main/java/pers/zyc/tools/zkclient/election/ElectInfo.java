package pers.zyc.tools.zkclient.election;

/**
 * @author zhangyancheng
 */
public class ElectInfo {
	private String electPath;
	private byte[] memberNodeData;
	private ElectMode electMode;

	public ElectInfo(String electPath, byte[] memberNodeData, ElectMode electMode) {
		this.electPath = electPath;
		this.memberNodeData = memberNodeData;
		this.electMode = electMode;
	}

	public String getElectPath() {
		return electPath;
	}

	public byte[] getMemberNodeData() {
		return memberNodeData;
	}

	public ElectMode getElectMode() {
		return electMode;
	}
}
