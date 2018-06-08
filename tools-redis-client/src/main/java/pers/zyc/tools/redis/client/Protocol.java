package pers.zyc.tools.redis.client;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangyancheng
 */
public class Protocol {

	static final Charset UTF8 = Charset.forName("UTF8");

	static final byte DOLLAR = '$';
	static final byte ASTERISK = '*';
	static final byte PLUS = '+';
	static final byte MINUS = '-';
	static final byte COLON = ':';

	private static final byte CR = '\r';
	private static final byte LF = '\n';

	private static final byte[] CRLF = new byte[] {CR, LF};

	private static final byte[] BYTES_TRUE = toByteArray(1);
	private static final byte[] BYTES_FALSE = toByteArray(0);

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

	static String bytesToString(byte[] strBytes) {
		return new String(strBytes, UTF8);
	}

	static Long bytesToLong(byte[] longBytes) {
		return Long.valueOf(bytesToString(longBytes));
	}

	static Double byteToDouble(byte[] doubleBytes) {
		return Double.valueOf(bytesToString(doubleBytes));
	}

	private static void writePart(ByteBuffer buffer, byte[] part) {
		byte[] partLengthByte = toByteArray(part.length);
		buffer.put(DOLLAR);
		buffer.put(partLengthByte);
		buffer.put(CRLF);
		buffer.put(part);
		buffer.put(CRLF);
	}

	/**
	 * [*号][消息元素个数]\r\n   (消息元素个数 = 参数个数 + 1个命令)
	 * [$号][命令字节个数]\r\n
	 * [命令内容]\r\n
	 * [$号][参数字节个数]\r\n
	 * [参数内容]\r\n           (最后两行循环参数个数次)
	 *
	 *
	 * @param buffer 请求字节缓存区
	 * @param cmd 请求命令字节
	 * @param args 请求part字节组
	 */
	static void encode(ByteBuffer buffer, byte[] cmd, byte[][] args) {
		byte[] lengthByte = toByteArray(1 + args.length);
		buffer.put(ASTERISK);
		buffer.put(lengthByte);
		buffer.put(CRLF);

		writePart(buffer, cmd);

		for (byte[] arg : args) {
			writePart(buffer, arg);
		}
	}

	private static void skipCRLF(ByteBuffer buffer) {
		assert buffer.get() == CR;
		assert buffer.get() == LF;
	}

	static Object decode(ByteBuffer buffer) {
		int len = buffer.limit();
		//响应包必然以\r\n结尾, 如果不是则响应数据未收完
		if (buffer.get(len - 1) != LF || buffer.get(len - 2) != CR) {
			throw new ResponseIncompleteException("Response packet not end with \\r\\n");
		}

		byte b = buffer.get();
		RType rType = RType.match(b);
		switch (rType) {
			case STATUS_CODE:
			case ERROR:
				return readLine(buffer);
			case INTEGER:
				return readLong(buffer);
			case BULK:
				return readBulk(buffer);
			case MULTI_BULK:
				return readMultiBulk(buffer);
			default:
				throw new RedisClientException("Unknown reply: " + (char) b);
		}
	}

	private static List<byte[]> readMultiBulk(ByteBuffer buffer) {
		long partLen = readLong(buffer);

		List<byte[]> ret = new ArrayList<>((int) partLen);
		if (partLen == 0) {
			return ret;
		}

		skipCRLF(buffer);

		while (buffer.hasRemaining()) {
			assert buffer.get() == DOLLAR;
			ret.add(readPart(buffer));
		}

		if (ret.size() == partLen) {
			return ret;
		}

		throw new ResponseIncompleteException("MultiBulk expect " + partLen + "part, but just received " + ret.size());
	}

	private static List<byte[]> readMultiBulk(byte[] bytes) {
		int offset = 1;
		byte[] partLenByte = getLongByte(bytes, offset);
		long partLen = bytesToLong(partLenByte);

		List<byte[]> ret = new ArrayList<>((int) partLen);
		if (partLen == 0) {
			return ret;
		}

		offset += (partLenByte.length + CRLF.length);
		while (bytes.length > offset) {
			offset++;
			partLenByte = getLongByte(bytes, offset);
			offset += (partLenByte.length + CRLF.length);

			byte[] partBytes = readPart(bytes, partLenByte, offset);
			ret.add(partBytes);
			if (partBytes != null) {
				offset += (partBytes.length + CRLF.length);
			}
		}

		if (ret.size() == partLen) {
			return ret;
		}

		throw new ResponseIncompleteException("MultiBulk expect " + partLen + "part, but just received " + ret.size());
	}

	private static byte[] readBulk(byte[] bytes) {
		int offset = 1;
		byte[] contentLenByte = getLongByte(bytes, offset);

		//offset增加(len bytes length + 一个\r\n)的长度
		offset += (contentLenByte.length + CRLF.length);

		return readPart(bytes, contentLenByte, offset);
	}

	private static byte[] readBulk(ByteBuffer buffer) {
		return readPart(buffer);
	}

	private static byte[] readPart(ByteBuffer buffer) {
		long contentLen = readLong(buffer);

		skipCRLF(buffer);

		if (!buffer.hasRemaining()) {
			if (contentLen == -1) {
				return null;
			} else {
				throw new ResponseIncompleteException("Bulk part data deficiency");
			}
		}

		//一定可以读取contentLen长度的内容
		return getContentByte(buffer, (int) contentLen);
	}

	private static byte[] readPart(byte[] bytes, byte[] contentLenByte, int offset) {
		long contentLen = bytesToLong(contentLenByte);
		if (bytes.length <= offset) {
			if (contentLen == -1) {
				return null;
			} else {
				throw new ResponseIncompleteException("Bulk part data deficiency");
			}
		} else {
			//一定可以读取contentLen长度的内容
			return getContentByte(bytes, offset, (int) contentLen);
		}
	}



	private static String readLine(ByteBuffer buffer) {
		return bytesToString(getContentByte(buffer, buffer.limit() - 3));
	}

	private static String readLine(byte[] bytes) {
		return bytesToString(getContentByte(bytes, 1, bytes.length - 3));
	}

	private static long readLong(ByteBuffer buffer) {
		return bytesToLong(getLongByte(buffer));
	}

	private static long readLong(byte[] bytes) {
		return bytesToLong(getLongByte(bytes, 1));
	}

	private static byte[] getLongByte(ByteBuffer buffer) {
		int len = 0;

		buffer.mark();
		while (buffer.get() != CR) {
			len++;
		}
		assert buffer.get() == LF;
		buffer.reset();

		return getContentByte(buffer, len);
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

	private static byte[] getContentByte(ByteBuffer buffer, int len) {
		byte[] ret = new byte[len];
		buffer.get(ret, 0, len);
		return ret;
	}

	private static byte[] getContentByte(byte[] bytes, int offset, int len) {
		byte[] ret = new byte[len];
		System.arraycopy(bytes, offset, ret, 0, len);
		return ret;
	}

	private enum RType {
		STATUS_CODE(PLUS),
		BULK(DOLLAR),
		MULTI_BULK(ASTERISK),
		INTEGER(COLON),
		ERROR(MINUS);

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
