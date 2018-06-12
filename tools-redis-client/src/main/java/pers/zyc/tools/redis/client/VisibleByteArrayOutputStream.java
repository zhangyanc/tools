package pers.zyc.tools.redis.client;

import java.io.ByteArrayOutputStream;

/**
 * @author zhangyancheng
 */
class VisibleByteArrayOutputStream extends ByteArrayOutputStream {

	VisibleByteArrayOutputStream() {
		super(8192);
	}

	byte[] reserveArray() {
		return buf;
	}
}
