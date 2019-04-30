package pers.zyc.tools.utils;

import java.net.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zhangyancheng
 */
public class IPUtil {
	
	public static byte[] toBytes(InetSocketAddress socketAddress) {
		return socketAddress.getAddress().getAddress();
	}

	public static byte[] toBytes(String ip) {
		String[] parts = ip.split("\\.");
		byte[] result = new byte[4];
		for (int i = 0; i < 4; i++) {
			result[i] = (byte) Integer.parseInt(parts[i]);
		}
		return result;
	}

	public static int toInt(String ip) {
		byte[] bytes = toBytes(ip);
		return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
	}

	public static String toIp(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 4; i++) {
			builder.append(String.valueOf(bytes[i] & 0xFF)).append('.');
		}
		return builder.substring(0, builder.length() - 1);
	}

	public static String toIp(int ip) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (ip >> 24);// ip & 0xFF000000 >> 24
		bytes[1] = (byte) (ip >> 16 & 0xFF);// ip & 0xFF0000 >> 16
		bytes[2] = (byte) (ip >> 8 & 0xFF);// ip & 0xFF00 >> 8
		bytes[3] = (byte) (ip);// ip & 0xFF >> 0
		return toIp(bytes);
	}

	public static Set<String> getAllLocalIP() throws SocketException {
		Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
		Set<String> result = new HashSet<>();
		while (networkInterfaces.hasMoreElements()) {
			NetworkInterface ni = networkInterfaces.nextElement();
			Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
			while (inetAddresses.hasMoreElements()) {
				InetAddress address = inetAddresses.nextElement();
				if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
					result.add(address.getHostAddress());
				}
			}
		}
		return result;
	}
}
