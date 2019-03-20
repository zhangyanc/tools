package pers.zyc.tools.jmxclient;

import com.sun.management.GarbageCollectorMXBean;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author zhangyancheng
 */
public class JmxTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(JmxTest.class);

	@Test
	public void case_0() {
		JmxHost host = new JmxHost("172.25.46.10", 10088);
		JmxUser user = new JmxUser("admin", "admin");

		JmxClient jmxClient = new JmxClient(host, user);
		LOGGER.info("{}", jmxClient.getConnection());

		List<GarbageCollectorMXBean> gcMXBeans = JvmStandardMXBeans.getGarbageCollectorMXBean(jmxClient);
		for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
			LOGGER.info("{}, {} {}", gcMXBean.getName(), gcMXBean.getCollectionCount(), gcMXBean.getCollectionTime());
		}
		jmxClient.close();
	}
}
