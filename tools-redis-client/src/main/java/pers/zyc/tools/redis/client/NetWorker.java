package pers.zyc.tools.redis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.lifecycle.PeriodicService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhangyancheng
 */
public class NetWorker extends PeriodicService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NetWorker.class);

	private final Selector selector = Selector.open();
	private final AtomicBoolean wakeUp = new AtomicBoolean(false);

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

	SocketNIO createSocket(String host, int port) throws IOException {
		SocketChannel channel = createChannel(host, port);
		SocketNIO socket = new SocketNIO(channel, this);

		synchronized (this) {
			wakeUp();
			channel.register(selector, SelectionKey.OP_READ, socket);
		}
		return socket;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		LOGGER.error("Uncaught exception", e);
		super.uncaughtException(t, e);
	}

	void cancel(SocketNIO socketNIO) {
		socketNIO.channel.keyFor(selector).cancel();
	}

	void switchWrite(SocketNIO socketNIO) {
		updateInterestOps(socketNIO.channel.keyFor(selector), SelectionKey.OP_WRITE);
		wakeUp();
	}

	void switchRead(SocketNIO socketNIO) {
		updateInterestOps(socketNIO.channel.keyFor(selector), SelectionKey.OP_READ);
	}

	private static void updateInterestOps(SelectionKey sk, int interestOps) {
		sk.interestOps(interestOps);
	}

	private void wakeUp() {
		if (wakeUp.compareAndSet(false, true)) {
			selector.wakeup();
		}
	}

	@Override
	protected long period() {
		return 0;
	}

	private void doSelect() {
		try {
			selector.select();

			if (wakeUp.compareAndSet(true, false)) {
				selector.selectNow();
			}
		} catch (IOException e) {
			throw new RedisClientException(e);
		}
	}

	@Override
	protected void execute() throws InterruptedException {
		doSelect();

		if (!isRunning()) {
			return;
		}

		Set<SelectionKey> selected;
		synchronized (this) {
			selected = selector.selectedKeys();
		}

		if (selected.isEmpty()) {
			return;
		}

		Iterator<SelectionKey> i = selected.iterator();

		while (i.hasNext()) {
			SelectionKey sk = i.next();
			i.remove();

			if (!sk.isValid()) {
				continue;
			}

			SocketNIO socket = (SocketNIO) sk.attachment();
			if (sk.isWritable()) {
				socket.write();
			}
			if (sk.isReadable()) {
				socket.read();
			}
		}
	}
}
