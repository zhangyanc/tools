/*
package pers.zyc.tools.redis.client;

import org.junit.Assert;
import org.junit.Test;
import pers.zyc.tools.redis.client.request.Append;
import pers.zyc.tools.redis.client.request.Get;
import pers.zyc.tools.redis.client.request.Set;

import java.nio.ByteBuffer;
import java.util.List;

*/
/**
 * @author zhangyancheng
 *//*

public class ProtocolTest {

	@Test
	public void case_decode_ResponseIncomplete$NotEndWithCRLF$MissCR_exceptionCaught() {
		try {
			byte[] notEndWithCRLFRespData = "+OK\n".getBytes(Protocol.UTF8);
			Protocol.decode(ByteBuffer.wrap(notEndWithCRLFRespData));
			Assert.fail();
		} catch (Exception e) {
			if (!(e instanceof ResponseIncompleteException)) {
				Assert.fail();
			}
		}
	}

	@Test
	public void case_decode_ResponseIncomplete$NotEndWithCRLF$MissLN_exceptionCaught() {
		try {
			byte[] notEndWithCRLFRespData = "+OK\r".getBytes(Protocol.UTF8);
			Protocol.decode(ByteBuffer.wrap(notEndWithCRLFRespData));
			Assert.fail();
		} catch (Exception e) {
			if (!(e instanceof ResponseIncompleteException)) {
				Assert.fail();
			}
		}
	}

	@Test
	public void case_decode_InvalidProtocol_exceptionCaught() {
		try {
			byte[] invalidRespData = "!OK\r\n".getBytes(Protocol.UTF8);
			Protocol.decode(ByteBuffer.wrap(invalidRespData));
			Assert.fail();
		} catch (Exception e) {
			if (!(e instanceof RedisClientException)) {
				Assert.fail();
			}
		}
	}

	@Test
	public void case_decode_ResponseIncomplete$BulkPartMiss_exceptionCaught() {
		try {
			byte[] bulkPartMissRespData = "$4\r\n".getBytes(Protocol.UTF8);
			Protocol.decode(ByteBuffer.wrap(bulkPartMissRespData));
			Assert.fail();
		} catch (Exception e) {
			if (!(e instanceof ResponseIncompleteException)) {
				Assert.fail();
			}
		}
	}

	@Test
	public void case_decode_ResponseIncomplete$MultiBulkPartMiss$1_exceptionCaught() {
		try {
			byte[] multibulkPartMissRespData = "*3\r\n$3\r\nSET\r\n$3\r\n".getBytes(Protocol.UTF8);
			Protocol.decode(ByteBuffer.wrap(multibulkPartMissRespData));
			Assert.fail();
		} catch (Exception e) {
			if (!(e instanceof ResponseIncompleteException)) {
				Assert.fail();
			}
		}
	}

	@Test
	public void case_decode_ResponseIncomplete$MultiBulkPartMiss$2_exceptionCaught() {
		try {
			byte[] multibulkPartMissRespData = "*3\r\n$3\r\nSET\r\n$3\r\nkey\r\n".getBytes(Protocol.UTF8);
			Protocol.decode(ByteBuffer.wrap(multibulkPartMissRespData));
			Assert.fail();
		} catch (Exception e) {
			if (!(e instanceof ResponseIncompleteException)) {
				Assert.fail();
			}
		}
	}

	@Test
	public void case_decode_StatusCode_success() {
		byte[] statusCodeRespData = "+OK\r\n".getBytes(Protocol.UTF8);
		Object resp = Protocol.decode(ByteBuffer.wrap(statusCodeRespData));
		Assert.assertTrue(resp instanceof String);
		Assert.assertEquals("OK", resp);
	}

	@Test
	public void case_decode_Error_success() {
		byte[] errorRespData = "-Some Error Message\r\n".getBytes(Protocol.UTF8);
		Object resp = Protocol.decode(ByteBuffer.wrap(errorRespData));
		Assert.assertTrue(resp instanceof String);
		Assert.assertEquals("Some Error Message", resp);
	}

	@Test
	public void case_decode_Integer_success() {
		byte[] integerRespData = ":1000\r\n".getBytes(Protocol.UTF8);
		Object resp = Protocol.decode(ByteBuffer.wrap(integerRespData));
		Assert.assertTrue(resp instanceof Long);
		Assert.assertEquals(1000L, resp);
	}

	@Test
	public void case_decode_Bulk_success() {
		byte[] bulkRespData = "$4\r\nBulk\r\n".getBytes(Protocol.UTF8);
		Object resp = Protocol.decode(ByteBuffer.wrap(bulkRespData));

		Assert.assertTrue(resp instanceof byte[]);
		Assert.assertEquals("Bulk", new String((byte[]) resp, Protocol.UTF8));
	}

	@Test
	public void case_decode_BulkChinese_success() {
		byte[] bulkRespData = "$10\r\nBulk张三\r\n".getBytes(Protocol.UTF8);
		Object resp = Protocol.decode(ByteBuffer.wrap(bulkRespData));

		Assert.assertTrue(resp instanceof byte[]);
		Assert.assertEquals("Bulk张三", new String((byte[]) resp, Protocol.UTF8));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void case_decode_MultiBulk_success() {
		//这是一条SET请求, 根据redis统一协议, 它同时也可以是一个响应
		byte[] multiBulkRespData = "*3\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n".getBytes(Protocol.UTF8);
		Object resp = Protocol.decode(ByteBuffer.wrap(multiBulkRespData));

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

		ByteBuffer buffer = ByteBuffer.allocate(8192);

		Protocol.encode(buffer, command, args);
		buffer.flip();

		byte[] protocolEncoded = new byte[buffer.remaining()];
		buffer.get(protocolEncoded);

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

		ByteBuffer buffer = ByteBuffer.allocate(8192);

		Protocol.encode(buffer, command, args);
		buffer.flip();

		byte[] protocolEncoded = new byte[buffer.remaining()];
		buffer.get(protocolEncoded);

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

		ByteBuffer buffer = ByteBuffer.allocate(8192);

		Protocol.encode(buffer, command, args);
		buffer.flip();

		byte[] protocolEncoded = new byte[buffer.remaining()];
		buffer.get(protocolEncoded);

		byte[] protocolBytes = "*6\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n$2\r\nNX\r\n$2\r\nEX\r\n$4\r\n1000\r\n".getBytes(Protocol.UTF8);
		byte[] jedisEncoded = JedisEncoder.encode(command, args);

		Assert.assertArrayEquals(protocolBytes, protocolEncoded);
		Assert.assertArrayEquals(jedisEncoded, protocolEncoded);
	}

	@Test
	public void case_encode_Get_success() {
		Request get = new Get("key");
		byte[] command = get.getCmd();
		byte[][] args = get.getArgs();

		ByteBuffer buffer = ByteBuffer.allocate(8192);

		Protocol.encode(buffer, command, args);
		buffer.flip();

		byte[] protocolEncoded = new byte[buffer.remaining()];
		buffer.get(protocolEncoded);

		byte[] protocolBytes = "*2\r\n$3\r\nGET\r\n$3\r\nkey\r\n".getBytes(Protocol.UTF8);
		byte[] jedisEncoded = JedisEncoder.encode(command, args);

		Assert.assertArrayEquals(protocolBytes, protocolEncoded);
		Assert.assertArrayEquals(jedisEncoded, protocolEncoded);
	}

	@Test
	public void case_encode_Append_success() {
		Request append = new Append("key", "value");

		byte[] command = append.getCmd();
		byte[][] args = append.getArgs();

		ByteBuffer buffer = ByteBuffer.allocate(8192);

		Protocol.encode(buffer, command, args);
		buffer.flip();

		byte[] protocolEncoded = new byte[buffer.remaining()];
		buffer.get(protocolEncoded);

		byte[] protocolBytes = "*3\r\n$6\r\nAPPEND\r\n$3\r\nkey\r\n$5\r\nvalue\r\n".getBytes(Protocol.UTF8);
		byte[] jedisEncoded = JedisEncoder.encode(command, args);

		Assert.assertArrayEquals(protocolBytes, protocolEncoded);
		Assert.assertArrayEquals(jedisEncoded, protocolEncoded);
	}
}
*/
