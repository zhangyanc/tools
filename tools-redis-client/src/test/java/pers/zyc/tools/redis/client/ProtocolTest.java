package pers.zyc.tools.redis.client;

import org.junit.Assert;
import org.junit.Test;
import pers.zyc.tools.redis.client.request.Append;
import pers.zyc.tools.redis.client.request.Get;
import pers.zyc.tools.redis.client.request.Set;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * @author zhangyancheng
 */
public class ProtocolTest {

	@Test
	public void case_decode_StatusCode_success() {
		byte[] statusCodeRespData = "+OK\r\n".getBytes(Protocol.UTF8);
		Object resp = Protocol.decode(statusCodeRespData);
		Assert.assertTrue(resp instanceof String);
		Assert.assertEquals("OK", resp);
	}

	@Test
	public void case_decode_Error_success() {
		byte[] errorRespData = "-Some Error Message\r\n".getBytes(Protocol.UTF8);
		Object resp = Protocol.decode(errorRespData);
		Assert.assertTrue(resp instanceof String);
		Assert.assertEquals("Some Error Message", resp);
	}

	@Test
	public void case_decode_Integer_success() {
		byte[] integerRespData = ":1000\r\n".getBytes(Protocol.UTF8);
		Object resp = Protocol.decode(integerRespData);
		Assert.assertTrue(resp instanceof Long);
		Assert.assertEquals(1000L, resp);
	}

	@Test
	public void case_decode_Bulk_success() {
		byte[] bulkRespData = "$4\r\nBulk\r\n".getBytes(Protocol.UTF8);
		Object resp = Protocol.decode(bulkRespData);

		Assert.assertTrue(resp instanceof byte[]);
		Assert.assertEquals("Bulk", new String((byte[]) resp, Protocol.UTF8));
	}

	@Test
	public void case_decode_BulkChinese_success() {
		byte[] bulkRespData = "$10\r\nBulk张三\r\n".getBytes(Protocol.UTF8);
		Object resp = Protocol.decode(bulkRespData);

		Assert.assertTrue(resp instanceof byte[]);
		Assert.assertEquals("Bulk张三", new String((byte[]) resp, Protocol.UTF8));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void case_decode_MultiBulk_success() {
		//这是一条SET请求, 根据redis统一协议, 它同时也可以是一个响应
		byte[] multiBulkRespData = "*3\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n".getBytes(Protocol.UTF8);
		Object resp = Protocol.decode(multiBulkRespData);

		Assert.assertTrue(resp instanceof List);

		List<byte[]> respByteList = (List<byte[]>) resp;

		Assert.assertTrue(respByteList.size() == 3);
		Assert.assertArrayEquals("SET".getBytes(Protocol.UTF8), respByteList.get(0));
		Assert.assertArrayEquals("key".getBytes(Protocol.UTF8), respByteList.get(1));
		Assert.assertArrayEquals("value".getBytes(Protocol.UTF8), respByteList.get(2));
	}

	@Test
	public void case_encode_Set1_success() {
		Request set = new Set("key", "value");
		byte[] command = set.getCmd();
		byte[][] args = set.getArgs();

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
		Request set = new Set("key", "value", "NX");
		byte[] command = set.getCmd();
		byte[][] args = set.getArgs();

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
		Request set = new Set("key", "value", "NX", "EX", 1000);
		byte[] command = set.getCmd();
		byte[][] args = set.getArgs();

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
		Request get = new Get("key");
		byte[] command = get.getCmd();
		byte[][] args = get.getArgs();

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
		Request append = new Append("key", "value");

		byte[] command = append.getCmd();
		byte[][] args = append.getArgs();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Protocol.encode(baos, command, args);

		byte[] encoded = baos.toByteArray();
		byte[] protocolBytes = "*3\r\n$6\r\nAPPEND\r\n$3\r\nkey\r\n$5\r\nvalue\r\n".getBytes(Protocol.UTF8);
		byte[] jedisEncoded = JedisEncoder.encode(command, args);

		Assert.assertArrayEquals(protocolBytes, encoded);
		Assert.assertArrayEquals(jedisEncoded, encoded);
	}
}
