package pers.zyc.tools.zkclient;

import java.util.Objects;

/**
 * @author zhangyancheng
 */
public class DefaultElector implements Elector {
	private byte[] memberData;
	private ElectionMode electionMode;

	public DefaultElector() {
		this(new byte[0], ElectionMode.MEMBER);
	}

	public DefaultElector(byte[] memberData, ElectionMode electionMode) {
		this.memberData = Objects.requireNonNull(memberData);
		this.electionMode = electionMode;
	}

	@Override
	public byte[] getMemberData() {
		return memberData;
	}

	public ElectionMode getElectionMode() {
		return electionMode;
	}
}
