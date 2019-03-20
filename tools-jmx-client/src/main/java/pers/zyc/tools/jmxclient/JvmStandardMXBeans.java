package pers.zyc.tools.jmxclient;

import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.OperatingSystemMXBean;
import com.sun.management.ThreadMXBean;

import javax.management.ObjectName;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 通过已连接到远程虚拟机的JmxClient获取虚拟机默认提供的标准的MXBean
 *
 * @author zhangyancheng
 */
public class JvmStandardMXBeans {

	/**
	 * 获取类加载MXBean
	 * @see java.lang.management.ClassLoadingMXBean
	 */
	public static ClassLoadingMXBean getClassLoadingMXBean(JmxClient jmxClient) {
		return jmxClient.getMBean(ClassLoadingMXBean.class, ManagementFactory.CLASS_LOADING_MXBEAN_NAME);
	}

	/**
	 * 获取内存MXBean
	 * @see java.lang.management.MemoryMXBean
	 */
	public static MemoryMXBean getMemoryMXBean(JmxClient jmxClient) {
		return jmxClient.getMBean(MemoryMXBean.class, ManagementFactory.MEMORY_MXBEAN_NAME);
	}

	/**
	 * 获取操作系统MXBean
	 * @see com.sun.management.OperatingSystemMXBean
	 */
	public static OperatingSystemMXBean getOperatingSystemMXBean(JmxClient jmxClient) {
		return jmxClient.getMBean(OperatingSystemMXBean.class, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
	}

	/**
	 * 获取RuntimeMXBean
	 * @see java.lang.management.RuntimeMXBean
	 */
	public static RuntimeMXBean getRuntimeMXBean(JmxClient jmxClient) {
		return jmxClient.getMBean(RuntimeMXBean.class, ManagementFactory.RUNTIME_MXBEAN_NAME);
	}

	/**
	 * 获取线程MXBean
	 * @see com.sun.management.ThreadMXBean
	 */
	public static ThreadMXBean getThreadMXBean(JmxClient jmxClient) {
		return jmxClient.getMBean(ThreadMXBean.class, ManagementFactory.THREAD_MXBEAN_NAME);
	}

	/**
	 * 获取垃圾收集器MXBean（一般是两个）
	 * @see com.sun.management.GarbageCollectorMXBean
	 */
	public static List<GarbageCollectorMXBean> getGarbageCollectorMXBean(JmxClient jmxClient) {
		String gcMXBeanNamePattern = ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*";
		Set<ObjectName> objectNames = jmxClient.queryNames(jmxClient.getObjectName(gcMXBeanNamePattern));
		List<GarbageCollectorMXBean> gcMxBeans = new ArrayList<>(objectNames.size());
		for (ObjectName objectName : objectNames) {
			gcMxBeans.add(jmxClient.getMBean(GarbageCollectorMXBean.class, objectName));
		}
		return gcMxBeans;
	}
}
