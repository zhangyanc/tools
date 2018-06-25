package pers.zyc.tools.redis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.*;
import pers.zyc.tools.redis.client.exception.RedisClientException;
import pers.zyc.tools.redis.client.exception.ResponseIncompleteException;
import pers.zyc.tools.redis.client.util.Future;
import pers.zyc.tools.redis.client.util.Promise;
import pers.zyc.tools.redis.client.util.ResponsePromise;
import pers.zyc.tools.utils.TimeMillis;
import sun.nio.ch.DirectBuffer;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static pers.zyc.tools.redis.client.util.ByteUtil.*;

/**
 * @author zhangyancheng
 */
class Connection implements EventSource<ConnectionEvent>, Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);
	private static final int DEFAULT_BUFFER_SIZE = 8192;

	final int id;
	final SocketChannel channel;
	boolean pooled = false;

	private final int requestTimeout;
	private final ByteBuffer buffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
	private final ResponseReceiveBuffer receiveBuffer = new ResponseReceiveBuffer(DEFAULT_BUFFER_SIZE);
	private final Multicaster<EventListener<ConnectionEvent>> multicaster =
			new Multicaster<EventListener<ConnectionEvent>>() {
		{
			setExceptionHandler(new MulticastExceptionHandler() {
				@Override
				public Void handleException(Throwable cause, MulticastDetail multicastDetail) {
					LOGGER.error(String.format("Multicast error: Event[%s], listener[%s]",
							multicastDetail.args[0], multicastDetail.listener), cause);
					return null;
				}
			});
		}
	};


	private Request request;
	private long timeoutLine;

	Connection(int id, SocketChannel channel, int requestTimeout) {
		this.id = id;
		this.channel = channel;
		this.requestTimeout = requestTimeout;
	}

	private static void closeChannel(SocketChannel channel) {
		try {
			channel.close();
		} catch (IOException ignored) {
		}
	}

	@Override
	public void addListener(EventListener<ConnectionEvent> listener) {
		multicaster.addListener(listener);
	}

	@Override
	public void removeListener(EventListener<ConnectionEvent> listener) {
		multicaster.removeListener(listener);
	}

	@Override
	public void close() {
		closeChannel(channel);
		((DirectBuffer) buffer).cleaner().clean();
		LOGGER.debug("Connection closed.");
		publishEvent(new ConnectionEvent.ConnectionClosed(this));
	}

	@Override
	public String toString() {
		return "Connection{id=" + id + ", channel=" + channel + "}";
	}

	private void publishEvent(ConnectionEvent event) {
		multicaster.listeners.onEvent(event);
	}

	<R> Future<R> send(Request request, final ResponseCast<R> responseCast) {
		this.request = request;
		LOGGER.debug("Request set.");

		Promise<R> promise = new ResponsePromise<>(responseCast);
		publishEvent(new ConnectionEvent.RequestSet(this, promise));

		return promise;
	}

	void write() {
		try {
			encodeAndWrite();
			LOGGER.debug("Request send.");
			timeoutLine = TimeMillis.get() + requestTimeout;
			publishEvent(new ConnectionEvent.RequestSend(this));
		} catch (Exception e) {
			publishEvent(new ConnectionEvent.ExceptionCaught(this, e));
		}
	}

	void read() {
		try {
			try {
				Object response = readAndDecode();
				receiveBuffer.reset();
				LOGGER.debug("Response received.");
				publishEvent(new ConnectionEvent.ResponseReceived(this, response));
			} catch (ResponseIncompleteException ignored) {
				LOGGER.debug("Response incomplete", ignored);
			}
		} catch (Exception e) {
			publishEvent(new ConnectionEvent.ExceptionCaught(this, e));
		}
	}

	boolean checkTimeout() {
		if (timeoutLine <= TimeMillis.get()) {
			LOGGER.debug("Request timeout.");
			publishEvent(new ConnectionEvent.RequestTimeout(this));
			return true;
		}
		return false;
	}























	//encode and decode

	private void encodeAndWrite() throws IOException {
		encodeIntCRLF(ASTERISK, request.partSize());

		byte[] part;
		while ((part = request.nextPart()) != null) {
			encodePartCRLF(part);
		}

		drainBuffer();
	}

	private void encodeIntCRLF(byte b, int length) throws IOException {
		byte[] intByte = toByteArray(length);
		need(intByte.length + 3);
		buffer.put(b);
		buffer.put(intByte);
		buffer.put(CRLF);
	}

	private void encodePartCRLF(byte[] part) throws IOException {
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

	private void need(int need) throws IOException {
		while (buffer.remaining() < need) {
			writeOnce();
		}
	}

	private void writeOnce() throws IOException {
		buffer.flip();
		channel.write(buffer);
		buffer.compact();
	}

	private void drainBuffer() throws IOException {
		buffer.flip();

		while (buffer.hasRemaining()) {
			channel.write(buffer);
		}

		buffer.clear();
	}

	private Object readAndDecode() throws IOException {
		int read;
		while ((read = channel.read(buffer)) > 0) {
			buffer.flip();
			while (buffer.hasRemaining()) {
				receiveBuffer.write(buffer.get());
			}
			buffer.clear();
		}

		if (read == -1) {
			throw new IOException("End of stream");
		}

		byte[] respBytes = receiveBuffer.reserveArray();
		int c = receiveBuffer.size();

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

	private static String readLine(ByteBuffer buffer) {
		return bytesToString(getContentByte(buffer, buffer.limit() - 3));
	}

	private static int readLength(ByteBuffer buffer) {
		return bytesToLong(getLongByte(buffer)).intValue();
	}

	private static long readInteger(ByteBuffer buffer) {
		return bytesToLong(getLongByte(buffer));
	}

	private static byte[] getLongByte(ByteBuffer buffer) {
		int len = 0;

		buffer.mark();
		while (buffer.get() != CR) {
			len++;
		}
		byte cr = buffer.get(); assert cr == CR;
		buffer.reset();

		return getContentByte(buffer, len);
	}

	private static byte[] getContentByte(ByteBuffer buffer, int len) {
		byte[] ret = new byte[len];
		buffer.get(ret, 0, len);
		return ret;
	}

	private static void skipCRLF(ByteBuffer buffer) {
		byte cr = buffer.get(), lf = buffer.get(); assert cr == CR; assert lf == LF;
	}

	private static class ResponseReceiveBuffer extends ByteArrayOutputStream {

		ResponseReceiveBuffer(int size) {
			super(size);
		}

		byte[] reserveArray() {
			return buf;
		}

		@Override
		public synchronized void reset() {
			//TODO shrink buf capacity
			super.reset();
		}
	}
}
