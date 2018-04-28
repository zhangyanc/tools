package pers.zyc.tools.zkclient;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author zhangyancheng
 */
public class ElectionTest extends BaseClientTest {

	private static final String ELECTION_PATH = "/election";

	@Test
	public void case_UnElect() throws Exception {
		ClientConfig config = new ClientConfig();
		config.setSyncStart(true);
		config.setConnectStr(CONNECT_STRING);

		createZKClient(config);

		zkSwitch.open();
		zkClient.start();

		LeaderElection election = zkClient.createElection();
		Assert.assertNull(election.leader());
		Assert.assertNull(election.member());
	}


}
