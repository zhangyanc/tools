package pers.zyc.tools.redis.client;

import pers.zyc.tools.event.Listenable;
import pers.zyc.tools.event.Multicaster;
import sun.nio.ch.DirectBuffer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static pers.zyc.tools.redis.client.Util.*;

/**
 * @author zhangyancheng
 */
class SocketNIO implements Closeable, Listenable<ResponseListener> {
	final SocketChannel channel;

	private final NetWorker netWorker;
	private final ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
	private final Multicaster<ResponseListener> multicaster = new Multicaster<ResponseListener>() {};

	private Encoder encoder;
	private Decoder decoder;

	SocketNIO(SocketChannel channel, NetWorker netWorker) {
		this.channel = channel;
		this.netWorker = netWorker;
	}

	@Override
	public void close() throws IOException {
		encoder = null;
		decoder = null;
		channel.close();
		netWorker.cancel(this);
		((DirectBuffer) buffer).cleaner().clean();
	}

	@Override
	public void addListener(ResponseListener listener) {
		multicaster.addListener(listener);
	}

	@Override
	public void removeListener(ResponseListener listener) {
		multicaster.removeListener(listener);
	}

	void request(Request request) {
		encoder = new Encoder(request);
		decoder = new Decoder();
		netWorker.switchWrite(this);
	}

	void write() {
		try {
			encoder.encodeAndWrite();
			netWorker.switchRead(this);
		} catch (Exception e) {
			multicaster.listeners.onSocketException(e);
		}
	}

	void read() {
		try {
			try {
				Object response = decoder.readAndDecode();
				multicaster.listeners.onResponseReceived(response);
			} catch (ResponseIncompleteException ignored) {
			}
		} catch (Exception e) {
			multicaster.listeners.onSocketException(e);
		}
	}

	private class Encoder {
		private final Request request;

		Encoder(Request request) {
			this.request = request;
		}

		void encodeAndWrite() throws IOException {
			encodeIntCRLF(ASTERISK, request.partSize());

			byte[] part;
			while ((part = request.nextPart()) != null) {
				encodePartCRLF(part);
			}

			drainBuffer();
		}

		void encodeIntCRLF(byte b, int length) throws IOException {
			byte[] intByte = toByteArray(length);
			need(intByte.length + 3);
			buffer.put(b);
			buffer.put(intByte);
			buffer.put(CRLF);
		}

		void encodePartCRLF(byte[] part) throws IOException {
			encodeIntCRLF(DOLLAR, part.length);

			ByteBuffer tmpMemBuffer = ByteBuffer.wrap(part);
			int writeRemain;
			while ((writeRemain = tmpMemBuffer.remaining()) > 0) {
				while (buffer.hasRemaining() && writeRemain-- > 0) {
					buffer.put(tmpMemBuffer.get());
				}
				if (!buffer.hasRemaining()) {
					writeOnce();
				}
			}

			need(2);
			buffer.put(CRLF);
		}

		void need(int need) throws IOException {
			while (buffer.remaining() < need) {
				writeOnce();
			}
		}

		void writeOnce() throws IOException {
			buffer.flip();
			channel.write(buffer);
			buffer.compact();
		}

		void drainBuffer() throws IOException {
			buffer.flip();

			while (buffer.hasRemaining()) {
				channel.write(buffer);
			}

			buffer.clear();
		}
	}

	private class Decoder {

		final VisibleByteArrayOutputStream vbaos = new VisibleByteArrayOutputStream();

		void readData() throws IOException {
			int read;
			while ((read = channel.read(buffer)) > 0) {
				buffer.flip();
				while (buffer.hasRemaining()) {
					vbaos.write(buffer.get());
				}
				buffer.clear();
			}

			if (read == -1) {
				throw new IOException("End of stream");
			}
		}

		Object readAndDecode() throws IOException {
			readData();

			byte[] respBytes = vbaos.reserveArray();
			int c = vbaos.size();

			if (respBytes[c - 2] != CR || respBytes[c - 1] != LF) {
				throw new ResponseIncompleteException("Response packet not end with \\r\\n");
			}

			ByteBuffer respBuffer = ByteBuffer.wrap(respBytes, 0, c);
			byte bType = respBuffer.get();
			switch (bType) {
				case PLUS:
				case MINUS:
					return readLine(respBuffer);
				case COLON:
					return readInteger(respBuffer);
				case DOLLAR:
					return readBulk(respBuffer);
				case ASTERISK:
					return readMultiBulk(respBuffer);
				default:
					throw new RedisClientException("Unknown reply: " + (char) bType);
			}
		}

	}

	private static byte[] readBulk(ByteBuffer buffer) {
		return readPart(buffer);
	}

	private static List<byte[]> readMultiBulk(ByteBuffer buffer) {
		int partLen = readLength(buffer);

		if (partLen == -1) {
			return null;
		}

		List<byte[]> ret = new ArrayList<>(partLen);
		if (partLen == 0) {
			return ret;
		}

		skipCRLF(buffer);

		while (buffer.hasRemaining()) {
			buffer.get();//assert buffer.get() == DOLLAR;
			ret.add(readPart(buffer));
		}

		if (ret.size() == partLen) {
			return ret;
		}

		throw new ResponseIncompleteException("MultiBulk expect " + partLen + "part, but just received " + ret.size());
	}

	private static byte[] readPart(ByteBuffer buffer) {
		int contentLen = readLength(buffer);

		skipCRLF(buffer);

		if (!buffer.hasRemaining()) {
			if (contentLen == -1) {
				return null;
			} else {
				throw new ResponseIncompleteException("Bulk part data deficiency");
			}
		}

		//一定可以读取contentLen长度的内容
		return getContentByte(buffer, contentLen);
	}

	private static byte[] readLine(ByteBuffer buffer) {
		return getContentByte(buffer, buffer.limit() - 3);
	}

	private static int readLength(ByteBuffer buffer) {
		return bytesToLong(getLongByte(buffer)).intValue();
	}

	private static byte[] readInteger(ByteBuffer buffer) {
		return getLongByte(buffer);
	}

	private static byte[] getLongByte(ByteBuffer buffer) {
		int len = 0;

		buffer.mark();
		while (buffer.get() != CR) {
			len++;
		}
		//assert buffer.get() == LF;
		buffer.reset();

		return getContentByte(buffer, len);
	}

	private static byte[] getContentByte(ByteBuffer buffer, int len) {
		byte[] ret = new byte[len];
		buffer.get(ret, 0, len);
		return ret;
	}

	private static void skipCRLF(ByteBuffer buffer) {
		buffer.get();//assert buffer.get() == CR
		buffer.get();//assert buffer.get() == LF
	}
}
