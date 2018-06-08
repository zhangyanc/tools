package pers.zyc.tools.redis.client;

import pers.zyc.tools.event.Listenable;
import pers.zyc.tools.event.Multicaster;
import sun.nio.ch.DirectBuffer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static pers.zyc.tools.redis.client.Protocol.encode;

/**
 * @author zhangyancheng
 */
class SocketNIO implements Closeable, Listenable<ResponseListener> {
	private final SelectionKey sk;
	private final NetWorker netWorker;
	private final SocketChannel channel;
	private final ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
	private final Multicaster<ResponseListener> multicaster = new Multicaster<ResponseListener>() {};

	SocketNIO(SelectionKey sk, NetWorker netWorker) {
		this.netWorker = netWorker;
		this.sk = sk;
		channel = (SocketChannel) sk.channel();
	}

	@Override
	public void close() throws IOException {
		sk.cancel();
		sk.channel().close();
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

	private void switchWrite() {
		sk.interestOps(SelectionKey.OP_WRITE);
	}

	private void switchRead() {
		sk.interestOps(SelectionKey.OP_READ);
	}

	SocketChannel channel() {
		return channel;
	}

	void request(byte[] cmd, byte[][] args) {
		try {
			buffer.clear();
			encode(buffer, cmd, args);
			buffer.flip();

			switchWrite();
			netWorker.wakeUp();
		} catch (Exception e) {
			multicaster.listeners.onSocketException(e);
		}
	}

	void write() {
		try {
			while (buffer.hasRemaining()) {
				channel.write(buffer);
			}
			buffer.clear();
			switchRead();
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
}
