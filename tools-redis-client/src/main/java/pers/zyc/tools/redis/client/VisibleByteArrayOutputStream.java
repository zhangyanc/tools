package pers.zyc.tools.redis.client;

import java.io.ByteArrayOutputStream;

/**
 * @author zhangyancheng
 */
class VisibleByteArrayOutputStream extends ByteArrayOutputStream {

	VisibleByteArrayOutputStream(int size) {
		super(size);
	}

	byte[] reserveArray() {
		return buf;
	}
}
