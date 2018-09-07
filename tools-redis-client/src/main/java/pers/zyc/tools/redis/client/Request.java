package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhangyancheng
 */
public abstract class Request<R> {

	/**
	 * 请求是否已结束(收到响应、超时、连接异常等)
	 */
	private final AtomicBoolean finished = new AtomicBoolean();

	/**
	 * 请求包块集合
	 */
	protected final LinkedList<byte[]> bulks = new LinkedList<>();

	protected Request(byte[]... bulks) {
		this.bulks.addAll(Arrays.asList(bulks));
		//加入命令字节块
		this.bulks.addFirst(getCmdBytes(getCommand()));
	}

	/**
	 * @return 命令字符串, 默认为子类名
	 */
	protected String getCommand() {
		return getClass().getSimpleName().toUpperCase();
	}

	/**
	 * 结束请求
	 *
	 * @return 首次结束请求时返回true, 否则返回false
	 */
	boolean finish() {
		return !finished.get() && finished.compareAndSet(false, true);
	}

	/**
	 * @return 获取响应转换器
	 */
	public abstract ResponseCast<R> getCast();

	@Override
	public String toString() {
		return "Request{" + getCommand() + "}";
	}

	/**
	 * 命令字节缓存
	 */
	private static final Map<String, byte[]> COMMAND_BYTES_CACHE_MAP = new ConcurrentHashMap<>();

	private static byte[] getCmdBytes(String command) {
		byte[] commandBytes = COMMAND_BYTES_CACHE_MAP.get(command);
		if (commandBytes == null) {
			//编码命令并缓存字节
			COMMAND_BYTES_CACHE_MAP.put(command, commandBytes = ByteUtil.toByteArray(command));
		}
		return commandBytes;
	}
}
