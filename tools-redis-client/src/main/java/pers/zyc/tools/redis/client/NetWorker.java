package pers.zyc.tools.redis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.lifecycle.PeriodicService;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

import static pers.zyc.tools.redis.client.Protocol.*;

/**
 * @author zhangyancheng
 */
public class NetWorker extends PeriodicService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NetWorker.class);

	private final Selector selector = Selector.open();

	NetWorker() throws IOException {
	}

	private static SocketChannel createChannel(String host, int port) throws IOException {
		SocketChannel sock = SocketChannel.open();
		try {
			sock.socket().setSoLinger(false, -1);
			sock.socket().setTcpNoDelay(true);
			sock.connect(new InetSocketAddress(host, port));
			sock.configureBlocking(false);
			return sock;
		} catch (IOException e) {
			try {
				sock.close();
			} catch (IOException ignored) {
			}
			throw e;
		}
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();
		selector.close();
	}

	Connection createConnection(String host, int port) throws IOException {
		SocketChannel channel = createChannel(host, port);

		SelectionKey sk = channel.register(selector, SelectionKey.OP_READ);
		Connection connection = new Connection(new SocketNIO(sk));
		sk.attach(connection);

		return connection;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		LOGGER.error("Uncaught exception", e);
		super.uncaughtException(t, e);
	}

	@Override
	protected long period() {
		return 0;
	}

	@Override
	protected void execute() throws InterruptedException {
		try {
			if (selector.select(60000) == 0) {
				return;
			}

			Set<SelectionKey> selected = selector.selectedKeys();
			for (SelectionKey sk : selected) {
				Connection connection = (Connection) sk.attachment();
				if (sk.isWritable()) {
					connection.writeRequest();
				}
				if (sk.isReadable()) {
					connection.readResponse();
				}
			}
		} catch (IOException e) {
			throw new RedisClientException(e);
		}
	}

	static class SocketNIO implements Closeable {
		private final SelectionKey sk;
		private final SocketChannel channel;
		private final ByteBuffer buffer = ByteBuffer.allocate(8192);
		private final ReserveByteArrayOutputStream baos = new ReserveByteArrayOutputStream(8192);

		SocketNIO(SelectionKey sk) {
			this.sk = sk;
			channel = (SocketChannel) sk.channel();
		}

		@Override
		public void close() throws IOException {
			sk.cancel();
			sk.channel().close();
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

		void enableWrite() {
			sk.interestOps(sk.interestOps() | SelectionKey.OP_WRITE);
		}

		void disableWrite() {
			sk.interestOps(sk.interestOps() & ~SelectionKey.OP_WRITE);
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

	private static class ReserveByteArrayOutputStream extends ByteArrayOutputStream {

		ReserveByteArrayOutputStream(int size) {
			super(size);
		}

		byte[] reserveArray() {
			return buf;
		}
	}
}
