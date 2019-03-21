package pers.zyc.tools.jmxclient;

import com.sun.management.GarbageCollectorMXBean;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.utils.event.EventListener;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author zhangyancheng
 */
public class JmxTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(JmxTest.class);

	@Test
	public void case_0() throws InterruptedException {
		JmxHost host = new JmxHost("172.25.46.10", 10088);
		JmxUser user = new JmxUser("admin", "admin");

		final CountDownLatch cdl = new CountDownLatch(2);
		JmxClient jmxClient = new JmxClient(host, user);
		jmxClient.addListener(new EventListener<ConnectionEvent>() {
			@Override
			public void onEvent(ConnectionEvent event) {
				cdl.countDown();
				LOGGER.info(event.toString());
			}
		});
		jmxClient.connect();

		List<GarbageCollectorMXBean> gcMXBeans = JvmStandardMXBeans.getGarbageCollectorMXBean(jmxClient);
		for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
			LOGGER.info("{}, {} {}", gcMXBean.getName(), gcMXBean.getCollectionCount(), gcMXBean.getCollectionTime());
		}
		jmxClient.disconnect();
		cdl.await();
	}

	@Test
	public void case_1() {
		JmxHost host = new JmxHost("172.25.46.10", 10088);
		JmxUser user = new JmxUser("admin", "admin");

		MemoryUsage memoryUsage = JmxTemplate.execute(host, user, new JmxCallback.ClientCallback<MemoryUsage>() {
			@Override
			public MemoryUsage call(JmxClient jmxClient) {
				MemoryMXBean memoryMXBean = JvmStandardMXBeans.getMemoryMXBean(jmxClient);
				return memoryMXBean.getHeapMemoryUsage();
			}
		});
		LOGGER.info("Max: {} Used: {}", memoryUsage.getMax(), memoryUsage.getUsed());

		Map<String, String> systemProperties = JmxTemplate.execute(host, user, RuntimeMXBean.class,
				ManagementFactory.RUNTIME_MXBEAN_NAME,
				new JmxCallback.MBeanCallback<RuntimeMXBean, Map<String, String>>() {
					@Override
					public Map<String, String> call(RuntimeMXBean runtimeMXBean) {
						return runtimeMXBean.getSystemProperties();
					}
				}
		);
		for (Map.Entry<String, String> entry : systemProperties.entrySet()) {
			LOGGER.info("{} -> {}", entry.getKey(), entry.getValue());
		}
	}
}
