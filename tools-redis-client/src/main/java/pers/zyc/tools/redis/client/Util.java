package pers.zyc.tools.redis.client;

import java.nio.charset.Charset;

/**
 * @author zhangyancheng
 */
public class Util {

	static final Charset UTF8 = Charset.forName("UTF8");

	static final byte DOLLAR = '$';
	static final byte ASTERISK = '*';
	static final byte PLUS = '+';
	static final byte MINUS = '-';
	static final byte COLON = ':';

	static final byte CR = '\r';
	static final byte LF = '\n';

	static final byte[] CRLF = new byte[] {CR, LF};

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

	static String bytesToString(byte[] strBytes) {
		return new String(strBytes, UTF8);
	}

	static Long bytesToLong(byte[] longBytes) {
		return Long.valueOf(bytesToString(longBytes));
	}

	static Double byteToDouble(byte[] doubleBytes) {
		return Double.valueOf(bytesToString(doubleBytes));
	}
}
