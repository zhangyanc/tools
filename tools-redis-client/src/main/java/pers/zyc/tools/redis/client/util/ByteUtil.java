package pers.zyc.tools.redis.client.util;

import java.nio.charset.Charset;

/**
 * @author zhangyancheng
 */
public class ByteUtil {

	public static final Charset UTF8 = Charset.forName("UTF8");

	public static final byte DOLLAR = '$';
	public static final byte ASTERISK = '*';
	public static final byte PLUS = '+';
	public static final byte MINUS = '-';
	public static final byte COLON = ':';

	public static final byte CR = '\r';
	public static final byte LF = '\n';

	public static final byte[] CRLF = new byte[] {CR, LF};

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

	public static String bytesToString(byte[] strBytes) {
		return new String(strBytes, UTF8);
	}

	public static Long bytesToLong(byte[] longBytes) {
		return Long.valueOf(bytesToString(longBytes));
	}

	public static Double byteToDouble(byte[] doubleBytes) {
		return Double.valueOf(bytesToString(doubleBytes));
	}
}
