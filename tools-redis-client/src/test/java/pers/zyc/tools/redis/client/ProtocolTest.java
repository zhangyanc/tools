package pers.zyc.tools.redis.client;

import org.junit.Assert;
import org.junit.Test;
import pers.zyc.tools.redis.client.request.Append;
import pers.zyc.tools.redis.client.request.Get;
import pers.zyc.tools.redis.client.request.Set;

import java.io.ByteArrayOutputStream;

/**
 * @author zhangyancheng
 */
public class ProtocolTest {

	@Test
	public void case_encode_Set1_success() {
		Set request = new Set("key", "value");
		byte[] command = request.getCmd();
		byte[][] args = request.getArgs();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Protocol.encode(baos, command, args);

		byte[] protocolEncoded = baos.toByteArray();
		byte[] manualEncoded = "*3\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n".getBytes(Protocol.UTF8);
		byte[] jedisEncoded = JedisEncoder.encode(command, args);

		Assert.assertArrayEquals(manualEncoded, protocolEncoded);
		Assert.assertArrayEquals(jedisEncoded, protocolEncoded);
	}

	@Test
	public void case_encode_Set2_success() {
		Set request = new Set("key", "value", "NX");
		byte[] command = request.getCmd();
		byte[][] args = request.getArgs();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Protocol.encode(baos, command, args);

		byte[] protocolEncoded = baos.toByteArray();
		byte[] manualEncoded = "*4\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n$2\r\nNX\r\n".getBytes(Protocol.UTF8);
		byte[] jedisEncoded = JedisEncoder.encode(command, args);

		Assert.assertArrayEquals(manualEncoded, protocolEncoded);
		Assert.assertArrayEquals(jedisEncoded, protocolEncoded);
	}

	@Test
	public void case_encode_Set3_success() {
		Set request = new Set("key", "value", "NX", "EX", 1000);
		byte[] command = request.getCmd();
		byte[][] args = request.getArgs();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Protocol.encode(baos, command, args);

		byte[] encoded = baos.toByteArray();
		byte[] protocolBytes = "*6\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n$2\r\nNX\r\n$2\r\nEX\r\n$4\r\n1000\r\n".getBytes(Protocol.UTF8);
		byte[] jedisEncoded = JedisEncoder.encode(command, args);

		Assert.assertArrayEquals(protocolBytes, encoded);
		Assert.assertArrayEquals(jedisEncoded, encoded);
	}

	@Test
	public void case_encode_Get_success() {
		Get request = new Get("key");
		byte[] command = request.getCmd();
		byte[][] args = request.getArgs();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Protocol.encode(baos, command, args);

		byte[] encoded = baos.toByteArray();
		byte[] protocolBytes = "*2\r\n$3\r\nGET\r\n$3\r\nkey\r\n".getBytes(Protocol.UTF8);
		byte[] jedisEncoded = JedisEncoder.encode(command, args);

		Assert.assertArrayEquals(protocolBytes, encoded);
		Assert.assertArrayEquals(jedisEncoded, encoded);
	}

	@Test
	public void case_encode_Append_success() {
		Append request = new Append("key", "value");

		byte[] command = request.getCmd();
		byte[][] args = request.getArgs();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Protocol.encode(baos, command, args);

		byte[] encoded = baos.toByteArray();
		byte[] protocolBytes = "*3\r\n$6\r\nAPPEND\r\n$3\r\nkey\r\n$5\r\nvalue\r\n".getBytes(Protocol.UTF8);
		byte[] jedisEncoded = JedisEncoder.encode(command, args);

		Assert.assertArrayEquals(protocolBytes, encoded);
		Assert.assertArrayEquals(jedisEncoded, encoded);
	}
}
