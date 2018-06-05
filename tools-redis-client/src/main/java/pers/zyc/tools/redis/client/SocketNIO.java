package pers.zyc.tools.redis.client;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static pers.zyc.tools.redis.client.Protocol.encode;

/**
 * @author zhangyancheng
 */
class SocketNIO implements Closeable {
	private static final int BUFFER_SIZE = 8192;

	private final SelectionKey sk;
	private final SocketChannel channel;
	private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	private final VisibleByteArrayOutputStream baos = new VisibleByteArrayOutputStream(BUFFER_SIZE);

	SocketNIO(SelectionKey sk) {
		this.sk = sk;
		channel = (SocketChannel) sk.channel();
	}

	@Override
	public void close() throws IOException {
		sk.cancel();
		sk.channel().close();
	}

	private void enableWrite() {
		sk.interestOps(sk.interestOps() | SelectionKey.OP_WRITE);
	}

	private void disableWrite() {
		sk.interestOps(sk.interestOps() & ~SelectionKey.OP_WRITE);
	}

	SocketChannel channel() {
		return channel;
	}

	void request(byte[] cmd, byte[][] args) {
		baos.reset();
		encode(baos, cmd, args);

		enableWrite();
		sk.selector().wakeup();
	}

	void write() throws IOException {
		byte[] writeData = baos.reserveArray();

		int remain, wroteLen = 0;
		while ((remain = writeData.length - wroteLen) > 0) {
			buffer.put(writeData, wroteLen, Math.min(remain, buffer.remaining()));

			buffer.flip();
			wroteLen += channel.write(buffer);
			buffer.compact();
		}

		baos.reset();
		buffer.clear();
		disableWrite();
	}

	Object read() throws IOException {
		while (channel.read(buffer) > 0) {
			buffer.flip();
			baos.write(buffer.array(), 0, buffer.remaining());
			buffer.clear();
		}

		return Protocol.decode(baos.reserveArray());
	}
}
