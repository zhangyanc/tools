package pers.zyc.tools.redis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.redis.client.exception.RedisClientException;
import pers.zyc.tools.redis.client.exception.ResponseIncompleteException;
import pers.zyc.tools.redis.client.request.server.ClientGetName;
import pers.zyc.tools.redis.client.request.server.ClientList;
import pers.zyc.tools.redis.client.request.server.ClientSetName;
import pers.zyc.tools.redis.client.request.connection.Auth;
import pers.zyc.tools.redis.client.request.connection.Ping;
import pers.zyc.tools.redis.client.request.connection.Quit;
import pers.zyc.tools.redis.client.request.connection.Select;
import pers.zyc.tools.redis.client.util.Promise;
import pers.zyc.tools.redis.client.util.ResponsePromise;
import pers.zyc.tools.utils.event.EventListener;
import pers.zyc.tools.utils.event.*;
import sun.nio.ch.DirectBuffer;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.*;

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
					LOGGER.error("Multicast error: " + multicastDetail, cause);
					return null;
				}
			});
		}
	};

	boolean allocated;
	boolean healthy = true;

	private Request<?> request;
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
		LOGGER.debug("Close {}", this);
		closeChannel(channel);
		((DirectBuffer) buffer).cleaner().clean();
		publishEvent(new ConnectionEvent.ConnectionClosed(this));
	}

	@Override
	public String toString() {
		return "Connection{channel=" + channel + "}";
	}

	private void publishEvent(ConnectionEvent event) {
		multicaster.listeners.onEvent(event);
	}

	/**
	 * 验证密码
	 *
	 * @param password 密码
	 */
	void auth(String password) {
		sync(new Auth(password));
	}

	/**
	 * 切换数据库
	 *
	 * @param dbIndex 数据库
	 */
	void selectDb(int dbIndex) {
		sync(new Select(dbIndex));
	}

	/**
	 * 测试网络
	 */
	void ping() {
		sync(new Ping());
	}

	/**
	 * 退出
	 */
	void quit() {
		sync(new Quit());
	}

	/**
	 * 设置连接名称
	 *
	 * @param clientName 连接名称
	 */
	void setClientName(String clientName) {
		sync(new ClientSetName(clientName));
	}

	/**
	 * @return 设置的连接名称(未设置返回空字符串)
	 */
	String getClientName() {
		return sync(new ClientGetName());
	}

	/**
	 * @see ClientList
	 */
	List<Map<String, String>> clientList() {
		String allClientStr = sync(new ClientList());

		BufferedReader br = new BufferedReader(new StringReader(allClientStr));
		List<Map<String, String>> clientList = new ArrayList<>();
		try {
			String line;
			while ((line = br.readLine()) != null) {
				String[] infoArr = line.split(" ");
				Map<String, String> clientInfoMap = new HashMap<>(infoArr.length);
				for (String info : infoArr) {
					String[] kv = info.split("=");
					clientInfoMap.put(kv[0], kv.length == 1 ? null : kv[1]);
				}
				clientList.add(clientInfoMap);
			}
			return clientList;
		} catch (Exception e) {
			throw new RedisClientException("Cannot cast CLIENT LIST response", e);
		}
	}

	private <R> R sync(Request<R> request) {
		return send(request).get();
	}

	/**
	 * 异步发送请求, 返回响应Future
	 *
	 * @param request 请求
	 * @param <R> 响应泛型
	 * @return 响应Future
	 */
	<R> Promise<R> send(Request<R> request) {
		return send(request, new ResponsePromise<>(request.getCast()));
	}

	/**
	 * 异步发送请求, 返回响应Future
	 *
	 * @param request 请求
	 * @param <R> 响应泛型
	 * @return 响应Future
	 */
	<R> Promise<R> send(Request<R> request, Promise<R> promise) {
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
		} catch (Exception e) {
			healthy = false;
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
		} catch (Exception e) {
			healthy = false;
			if (!allocated || request.finish()) {
				publishEvent(new ConnectionEvent.ExceptionCaught(this, e));
			}
		}
	}

	void timeout() {
		LOGGER.debug("{} timeout.", this.request);
		healthy = false;
		if (request.finish()) {
			publishEvent(new ConnectionEvent.RequestTimeout(this));
		}
	}





















	//encode and decode

	/**
	 * 编码并写出请求数据
	 *
	 * @throws IOException 网络异常
	 */
	private void encodeAndWrite() throws IOException {
		encodeIntCRLF(ASTERISK, request.bulks.size());


		byte[] bulk;
		while ((bulk = request.bulks.poll()) != null) {
			encodeIntCRLF(DOLLAR, bulk.length);

			int writeIndex = 0;
			do {
				need(1);//需要buffer非满
				int writeLen = Math.min(buffer.remaining(), bulk.length - writeIndex);
				buffer.put(bulk, writeIndex, writeLen);
				writeIndex += writeLen;
				//循环直到bulk数据全部处理
			} while (writeIndex < bulk.length);

			need(CRLF.length);
			buffer.put(CRLF);
		}

		//排空buffer
		buffer.flip();
		while (buffer.hasRemaining()) {
			channel.write(buffer);
		}
		buffer.clear();
	}

	/**
	 * 写块长度
	 *
	 * @param b 符号
	 * @param length 长度
	 * @throws IOException 网络异常
	 */
	private void encodeIntCRLF(byte b, int length) throws IOException {
		byte[] intByte = toByteArray(length);
		need(intByte.length + CRLF.length + 1);
		buffer.put(b);
		buffer.put(intByte);
		buffer.put(CRLF);
	}

	/**
	 * buffer处于写模式时, put前确保有足够的剩余可写
	 *
	 * @param need 需要写入大小
	 * @throws IOException 网络异常
	 */
	private void need(int need) throws IOException {
		while (buffer.remaining() < need) {
			buffer.flip();
			channel.write(buffer);
			buffer.compact();
		}
	}

	/**
	 * 读取响应数据并解码
	 *
	 * @return 响应
	 * @throws IOException 网络异常
	 * @throws ResponseIncompleteException 数据未完整
	 */
	private Object readAndDecode() throws IOException {
		if (responseBuffer == null) {
			responseBuffer = new byte[32];
			responseBytesCount = 0;
		}

		int read;
		while ((read = channel.read(buffer)) > 0) {
			int needCapacity = read + responseBytesCount;
			if (needCapacity > responseBuffer.length) {
				responseBuffer = Arrays.copyOf(responseBuffer,
						Math.max(responseBuffer.length * 2, needCapacity));
			}
			//缓存数据复制到response buffer数组, 并累计数组写入位置
			buffer.flip();
			buffer.get(responseBuffer, responseBytesCount, read);
			buffer.clear();
			responseBytesCount += read;
		}

		try {
			//Redis数据包至少3个字节且必定以\r\n结尾
			if (responseBytesCount < 3 ||
				responseBuffer[responseBytesCount - 2] != CR ||
				responseBuffer[responseBytesCount - 1] != LF) {
				throw new ResponseIncompleteException("Response packet not end with \\r\\n");
			}

			return decodeResp(ByteBuffer.wrap(responseBuffer, 0, responseBytesCount));
		} catch (ResponseIncompleteException rie) {
			if (read != -1) {
				throw rie;
			}
			//响应数据不完整但流已关闭
			throw new IOException("Unexpected end of stream");
		}
	}

	private static Object decodeResp(ByteBuffer byteBuffer) {
		byte bType = byteBuffer.get();
		switch (bType) {
			case PLUS:
			case MINUS:
				return readLine(byteBuffer);
			case COLON:
				return readInteger(byteBuffer);
			case DOLLAR:
				return readBulk(byteBuffer);
			case ASTERISK:
				return readMultiBulk(byteBuffer);
			default:
				throw new RedisClientException("Unknown reply: " + bType);
		}
	}


	private static List<Object> readMultiBulk(ByteBuffer buffer) {
		int bulks = readLength(buffer);

		if (bulks == -1) {
			return null;
		}

		List<Object> ret = new ArrayList<>(bulks);
		if (bulks == 0) {
			return ret;
		}

		skipCRLF(buffer);

		while (buffer.hasRemaining()) {
			ret.add(decodeResp(buffer));

			if (buffer.hasRemaining()) {
				skipCRLF(buffer);
			}
		}

		if (ret.size() == bulks) {
			return ret;
		}

		throw new ResponseIncompleteException("MultiBulk expect " + bulks + " bulk, but received " + ret.size());
	}

	private static byte[] readBulk(ByteBuffer buffer) {
		int bulkLen = readLength(buffer);

		skipCRLF(buffer);

		if (!buffer.hasRemaining()) {
			if (bulkLen == -1) {
				return null;
			} else {
				throw new ResponseIncompleteException("Bulk data deficiency");
			}
		}

		//一定可以读取contentLen长度的内容
		return getContentByte(buffer, bulkLen);
	}

	/**
	 * 读状态
	 *
	 * @param buffer 响应buffer
	 * @return 状态字符串
	 */
	private static String readLine(ByteBuffer buffer) {
		return bytesToString(getContentByte(buffer, buffer.remaining() - 2));
	}

	private static int readLength(ByteBuffer buffer) {
		return bytesToLong(getLongByte(buffer)).intValue();
	}

	/**
	 * 读整数
	 *
	 * @param buffer 响应buffer
	 * @return 整数
	 */
	private static long readInteger(ByteBuffer buffer) {
		return bytesToLong(getLongByte(buffer));
	}

	/**
	 * 读取块长度
	 *
	 * @param buffer 响应buffer
	 * @return 块长度字节
	 */
	private static byte[] getLongByte(ByteBuffer buffer) {
		int len = 0;

		//先标记位置, 读到\r\n后重置, 累计的长度即为块长度的字节数
		buffer.mark();
		while (buffer.get() != CR) {
			len++;
		}
		byte lf = buffer.get(); assert lf == LF;
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
