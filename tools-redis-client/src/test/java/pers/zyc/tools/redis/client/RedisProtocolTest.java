package pers.zyc.tools.redis.client;

import redis.clients.util.RedisOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author zhangyancheng
 */
public class RedisProtocolTest {

	public static void main(String[] args) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RedisOutputStream ros = new RedisOutputStream(baos);

		String commandLine = "set ka abc de f";
		String[] commandParts = commandLine.split(" ", 2);

		Protocol.sendCommand(ros, string2Byte(commandParts[0]), cl2byteArray(commandParts[1]));
		ros.flush();

		byte[] realBytes = baos.toByteArray();

		System.out.println(new String(realBytes, Charset.forName("UTF8")));
	}

	private static byte[] string2Byte(String string) {
		return string.getBytes(Charset.forName("UTF8"));
	}

	private static byte[][] cl2byteArray(String vals) {
		String[] parts = vals.split(" ");
		byte[][] ret = new byte[parts.length][];
		for (int i = 0; i < parts.length; i++) {
			ret[i] = string2Byte(parts[i]);
		}
		return ret;
	}
}
