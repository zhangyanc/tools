package pers.zyc.tools.redis.client;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

/**
 * @author zhangyancheng
 */
public class Protocol {

	public static final Charset UTF8 = Charset.forName("UTF8");

	public static final byte DOLLAR_BYTE = '$';
	public static final byte ASTERISK_BYTE = '*';
	public static final byte PLUS_BYTE = '+';
	public static final byte MINUS_BYTE = '-';
	public static final byte COLON_BYTE = ':';

	public static final byte CR = '\r';
	public static final byte LF = '\n';

	public static final byte[] BYTES_TRUE = toByteArray(1);
	public static final byte[] BYTES_FALSE = toByteArray(0);

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
}
