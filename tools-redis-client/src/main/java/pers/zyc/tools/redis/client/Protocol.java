package pers.zyc.tools.redis.client;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangyancheng
 */
public class Protocol {

	static final Charset UTF8 = Charset.forName("UTF8");

	static final byte DOLLAR_BYTE = '$';
	static final byte ASTERISK_BYTE = '*';
	static final byte PLUS_BYTE = '+';
	static final byte MINUS_BYTE = '-';
	static final byte COLON_BYTE = ':';

	static final byte CR = '\r';
	static final byte LF = '\n';

	static final byte[] BYTES_TRUE = toByteArray(1);
	static final byte[] BYTES_FALSE = toByteArray(0);

	public static byte[] toByteArray(String str) {
		return str.getBytes(UTF8);
	}

	public static byte[] toByteArray(int i) {
		return Integer.toString(i).getBytes(UTF8);
	}

	public static byte[] toByteArray(long l) {
		return Long.toString(l).getBytes(UTF8);
	}

	public static byte[] toByteArray(boolean b) {
		return b ? BYTES_TRUE : BYTES_FALSE;
	}

	public static byte[] toByteArray(double d) {
		return Double.toString(d).getBytes(UTF8);
	}

	private static String bytesToString(byte[] strBytes) {
		return new String(strBytes, UTF8);
	}

	private static long bytesToLong(byte[] longBytes) {
		return Long.parseLong(bytesToString(longBytes));
	}

	private static void writeCRLF(ByteArrayOutputStream baos) {
		baos.write(CR);
		baos.write(LF);
	}

	private static void writePart(ByteArrayOutputStream baos, byte[] part) {
		byte[] partLengthByte = toByteArray(part.length);
		baos.write(DOLLAR_BYTE);
		baos.write(partLengthByte, 0, partLengthByte.length);
		writeCRLF(baos);

		baos.write(part, 0, part.length);
		writeCRLF(baos);
	}

	/**
	 * [*号][消息元素个数]\r\n   (消息元素个数 = 参数个数 + 1个命令)
	 * [$号][命令字节个数]\r\n
	 * [命令内容]\r\n
	 * [$号][参数字节个数]\r\n
	 * [参数内容]\r\n           (最后两行循环参数个数次)
	 *
	 * @param cmd cmd
	 * @param args args
	 */
	static void encode(ByteArrayOutputStream baos, byte[] cmd, byte[][] args) {
		byte[] lengthByte = toByteArray(1 + args.length);
		baos.write(ASTERISK_BYTE);
		baos.write(lengthByte, 0, lengthByte.length);
		writeCRLF(baos);

		writePart(baos, cmd);

		for (byte[] arg : args) {
			writePart(baos, arg);
		}
	}

	static Object decode(byte[] responseData) {
		int len = responseData.length;
		//响应包必然以\r\n结尾, 如果不是则响应数据未收完
		if (responseData[len - 1] != LF || responseData[len - 2] != CR) {
			return null;
		}

		final byte b = responseData[0];
		RType rType = RType.match(b);
		switch (rType) {
			case STATUS_CODE:
			case ERROR:
				return readLine(responseData);
			case INTEGER:
				return readLong(responseData);
			case BULK:
				return readBulk(responseData);
			case MULTI_BULK:
				return readMultiBulk(responseData);
			default:
				throw new RedisClientException("Unknown reply: " + (char) b);
		}
	}

	private static List<byte[]> readMultiBulk(byte[] bytes) {
		List<byte[]> ret = new ArrayList<>();

		int offset = 1;
		byte[] partLenByte = getLongByte(bytes, offset);
		long partLen = bytesToLong(partLenByte);
		if (partLen == 0) {
			return ret;
		}

		offset += (2 + partLenByte.length);
		while (bytes.length > offset) {
			offset++;
			partLenByte = getLongByte(bytes, offset);
			offset += (2 + partLenByte.length);

			if (bytes.length <= offset) {
				//数据未收完
				return null;
			}

			byte[] partBytes = readPart(bytes, partLenByte, offset);
			ret.add(partBytes);
			offset += (2 + partBytes.length);
		}

		//未读到足够的part返回null
		return ret.size() == partLen ? ret : null;
	}

	private static byte[] readBulk(byte[] bytes) {
		int offset = 1;
		byte[] contentLenByte = getLongByte(bytes, offset);
		//offset增加(len bytes length + 一个\r\n)的长度
		offset += (contentLenByte.length + 2);

		return bytes.length <= offset ? null : readPart(bytes, contentLenByte, offset);
	}

	private static byte[] readPart(byte[] bytes, byte[] contentLenByte, int offset) {
		long contentLen = bytesToLong(contentLenByte);
		if (contentLen == -1) {
			return contentLenByte;
		}
		//一定可以读取contentLen长度的内容
		return getContentByte(bytes, offset, (int) contentLen);
	}

	private static String readLine(byte[] bytes) {
		return bytesToString(getContentByte(bytes, 1, bytes.length - 3));
	}

	private static long readLong(byte[] bytes) {
		return bytesToLong(getLongByte(bytes, 1));
	}

	private static byte[] getLongByte(byte[] bytes, int offset) {
		int end = 0;
		for (int i = offset; i < bytes.length - 1; i++) {
			if (bytes[i] == CR && bytes[i + 1] == LF) {
				end = i;
				break;
			}
		}
		return getContentByte(bytes, offset, end - offset);
	}

	private static byte[] getContentByte(byte[] bytes, int offset, int len) {
		byte[] ret = new byte[len];
		System.arraycopy(bytes, offset, ret, 0, len);
		return ret;
	}

	private enum RType {
		STATUS_CODE(PLUS_BYTE),
		BULK(DOLLAR_BYTE),
		MULTI_BULK(ASTERISK_BYTE),
		INTEGER(COLON_BYTE),
		ERROR(MINUS_BYTE);

		private byte b;

		RType(byte b) {
			this.b = b;
		}

		static RType match(byte b) {
			for (RType t : values()) {
				if (t.b == b) {
					return t;
				}
			}
			throw new RedisClientException("Unknown reply: " + (char) b);
		}
	}
}
