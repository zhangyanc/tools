package pers.zyc.tools.redis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.redis.client.exception.RedisClientException;
import pers.zyc.tools.redis.client.exception.ResponseIncompleteException;
import pers.zyc.tools.redis.client.util.Promise;
import pers.zyc.tools.redis.client.util.ResponsePromise;
import pers.zyc.tools.utils.event.*;
import sun.nio.ch.DirectBuffer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pers.zyc.tools.redis.client.util.ByteUtil.*;

/**
 * @author zhangyancheng
 */
class Connection implements EventSource<ConnectionEvent>, Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);

	private final SelectionKey sk;
	private final NetWorker netWorker;
	private final SocketChannel channel;
	private final ByteBuffer buffer;
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

	boolean allocated;
	boolean broken;
	private Request request;
	private byte[] responseBuffer;
	private int responseBytesCount;

	Connection(SocketChannel channel, NetWorker netWorker) throws IOException {
		this.channel = channel;
		this.netWorker = netWorker;

		try {
			sk = netWorker.register(channel);
			sk.attach(this);
		} catch (IOException e) {
			channel.close();
			throw e;
		}
		buffer = ByteBuffer.allocateDirect(8192);
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
		sk.cancel();
		closeChannel(channel);
		((DirectBuffer) buffer).cleaner().clean();
		publishEvent(new ConnectionEvent.ConnectionClosed(this));
		LOGGER.debug("{} closed.", this);
	}

	@Override
	public String toString() {
		return "Connection{channel=" + channel + "}";
	}

	private void publishEvent(ConnectionEvent event) {
		multicaster.listeners.onEvent(event);
	}

	/**
	 * 异步发送请求, 返回响应Future
	 *
	 * @param request 请求
	 * @param responseCast 响应转换
	 * @param <R> 响应泛型
	 * @return 响应Future
	 */
	<R> Promise<R> send(Request request, final ResponseCast<R> responseCast) {
		Promise<R> promise = new ResponsePromise<>(responseCast);
		publishEvent(new ConnectionEvent.RequestSet(this, promise));

		this.request = request;
		enableWrite();

		LOGGER.debug("{} set.", request);
		return promise;
	}

	private void enableWrite() {
		sk.interestOps(sk.interestOps() | SelectionKey.OP_WRITE);
		netWorker.wakeUp();
	}

	private void disableWrite() {
		sk.interestOps(sk.interestOps() & (~SelectionKey.OP_WRITE));
	}

	/**
	 * NetWorker调用, 写出请求, 在Request数据全部写出后才返回
	 */
	void write() {
		try {
			encodeAndWrite();
			disableWrite();

			LOGGER.debug("{} send.", this.request);
			publishEvent(new ConnectionEvent.RequestSend(this));
		} catch (IOException e) {
			broken = true;
			if (request.finish()) {
				publishEvent(new ConnectionEvent.ExceptionCaught(this, e));
			}
		}
	}

	/**
	 * NetWorker调用, 读取响应, 单个响应可能有多次read调用
	 */
	void read() {
		try {
			Object response = readAndDecode();
			responseBuffer = null;

			LOGGER.debug("{} Response received.", this.request);
			if (request.finish()) {
				publishEvent(new ConnectionEvent.ResponseReceived(this, response));
			}
		} catch (ResponseIncompleteException ignored) {
		} catch (IOException e) {
			broken = true;
			if (!allocated || request.finish()) {
				publishEvent(new ConnectionEvent.ExceptionCaught(this, e));
			}
		}
	}

	void timeout() {
		LOGGER.debug("{} timeout.", this.request);
		if (request.finish()) {
			publishEvent(new ConnectionEvent.RequestTimeout(this));
		}
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

	private void readToResponseBuffer() throws IOException {
		if (responseBuffer == null) {
			responseBuffer = new byte[256];
			responseBytesCount = 0;
		}

		int read;
		while ((read = channel.read(buffer)) > 0) {
			int needCapacity = read + responseBytesCount;
			if (needCapacity > responseBuffer.length) {
				responseBuffer = Arrays.copyOf(responseBuffer, Math.max(responseBuffer.length * 2, needCapacity));
			}
			buffer.flip();
			buffer.get(responseBuffer, responseBytesCount, read);
			buffer.clear();
			responseBytesCount += read;
		}

		if (read == -1) {
			throw new IOException("End of stream");
		}
	}

	private Object readAndDecode() throws IOException {
		readToResponseBuffer();

		if (responseBuffer[responseBytesCount - 2] != CR || responseBuffer[responseBytesCount - 1] != LF) {
			throw new ResponseIncompleteException("Response packet not end with \\r\\n");
		}

		ByteBuffer respBuffer = ByteBuffer.wrap(responseBuffer, 0, responseBytesCount);
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
			byte $ = buffer.get(); assert $ == DOLLAR;
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
		byte cr = buffer.get(); assert cr == LF;
		buffer.reset();

		return getContentByte(buffer, len);
	}

	private static byte[] getContentByte(ByteBuffer buffer, int len) {
		byte[] ret = new byte[len];
		buffer.get(ret, 0, len);
		return ret;
	}

	private static void skipCRLF(ByteBuffer buffer) {
		byte cr = buffer.get(), lf = buffer.get();
		assert cr == CR; assert lf == LF;
	}
}
