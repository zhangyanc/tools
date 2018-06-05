package pers.zyc.tools.redis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.lifecycle.PeriodicService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

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
}
