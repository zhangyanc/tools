package pers.zyc.tools.redis.client;

import pers.zyc.tools.event.Listenable;
import pers.zyc.tools.event.Multicaster;
import sun.nio.ch.DirectBuffer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static pers.zyc.tools.redis.client.Protocol.*;

/**
 * @author zhangyancheng
 */
class SocketNIO implements Closeable, Listenable<ResponseListener> {
	final SocketChannel channel;

	private final NetWorker netWorker;
	private final ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
	private final Multicaster<ResponseListener> multicaster = new Multicaster<ResponseListener>() {};

	private Request request;

	SocketNIO(SocketChannel channel, NetWorker netWorker) {
		this.channel = channel;
		this.netWorker = netWorker;
	}

	@Override
	public void close() throws IOException {
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
		this.request = request;
		netWorker.switchWrite(this);
	}

	void write() {
		try {
			new Encoder(request).encodeAndWrite();
			netWorker.switchRead(this);
		} catch (Exception e) {
			multicaster.listeners.onSocketException(e);
		}
	}

	void read() {
		try {
			int readSize;
			do {
				readSize = channel.read(buffer);
				if (readSize == -1) {
					//TODO channel closed
				}
			} while (readSize > 0);

			readSize = buffer.position();//记录总共写入位置
			buffer.flip();

			try {
				Object response = Protocol.decode(buffer);
				multicaster.listeners.onResponseReceived(response);
			} catch (ResponseIncompleteException rie) {
				//响应数据包未收完, 需重置buffer写入位置继续读channel
				buffer.position(readSize);
				buffer.limit(buffer.capacity());
			}
		} catch (Exception e) {
			multicaster.listeners.onSocketException(e);
		}
	}

	private class Encoder {
		private final Request request;

		Encoder(Request request) {
			this.request = request;
			buffer.clear();
		}

		void encodeAndWrite() throws IOException {
			encodeIntCRLF(ASTERISK, request.partSize());

			byte[] part;
			while ((part = request.nextPart()) != null) {
				encodePartCRLF(part);
			}

			drainBuffer();
		}

		void encodeCRLF() throws IOException {
			need(2);
			buffer.put(CRLF);
		}

		void encodeIntCRLF(byte b, int length) throws IOException {
			byte[] intByte = toByteArray(length);
			need(intByte.length + 1);
			buffer.put(b);
			buffer.put(intByte);
			encodeCRLF();
		}

		void encodePartCRLF(byte[] part) throws IOException {
			encodeIntCRLF(DOLLAR, part.length);
			writePart(part);
			encodeCRLF();
		}

		void writePart(byte[] partData) throws IOException {
			ByteBuffer tmpMemBuffer = ByteBuffer.wrap(partData);
			int writeRemain;
			while ((writeRemain = tmpMemBuffer.remaining()) > 0) {
				while (buffer.hasRemaining() && writeRemain-- > 0) {
					buffer.put(tmpMemBuffer.get());
				}
				if (!buffer.hasRemaining()) {
					writeOnce();
				}
			}
		}

		void need(int need) throws IOException {
			while (buffer.remaining() < need) writeOnce();
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
		}
	}

	private class Decoder {

		Decoder() {
			buffer.clear();
		}


	}
}
