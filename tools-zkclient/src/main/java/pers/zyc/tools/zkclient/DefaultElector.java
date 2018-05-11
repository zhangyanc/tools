package pers.zyc.tools.zkclient;

import java.util.Objects;

/**
 * @author zhangyancheng
 */
public class DefaultElector implements Elector {
	private byte[] memberData;
	private ElectorMode electorMode;

	public DefaultElector() {
		this(new byte[0], ElectorMode.FOLLOWER);
	}

	public DefaultElector(byte[] memberData, ElectorMode electorMode) {
		this.memberData = Objects.requireNonNull(memberData);
		this.electorMode = electorMode;
	}

	@Override
	public byte[] getMemberData() {
		return memberData;
	}

	public ElectorMode getElectorMode() {
		return electorMode;
	}
}
