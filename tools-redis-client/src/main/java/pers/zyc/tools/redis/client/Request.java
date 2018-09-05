package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.util.ByteUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhangyancheng
 */
public abstract class Request<R> implements ResponseCast<R> {

	/**
	 * 请求是否已结束(收到响应、超时、连接异常等)
	 */
	private final AtomicBoolean finished = new AtomicBoolean();

	/**
	 * 响应结果转换
	 */
	private final ResponseCast<R> cast;

	/**
	 * 请求包块集合
	 */
	protected final LinkedList<byte[]> bulks = new LinkedList<>();

	@SuppressWarnings("unchecked")
	protected Request(byte[]... bulks) {
		this.bulks.addAll(Arrays.asList(bulks));
		//加入命令字节块
		this.bulks.addFirst(getCmdBytes(getCommand()));

		Type genericType = ((ParameterizedType)
				getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		//通过子类标记的泛型类型获取对应的响应转换器
		cast = (ResponseCast<R>) getCastByteGenericType(genericType);
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

	@Override
	public R cast(Object response) {
		return cast.cast(response);
	}

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

	/**
	 * 泛型与转换映射
	 */
	private static final Map<Type, ResponseCast<?>> RESPONSE_CAST_MAP = new ConcurrentHashMap<>();

	private static ResponseCast<?> getCastByteGenericType(Type genericType) {
		if (genericType == null) {
			return null;
		}
		ResponseCast<?> responseCast = RESPONSE_CAST_MAP.get(genericType);
		if (responseCast != null) {
			return responseCast;
		}

		if (genericType instanceof ParameterizedType) {
			Class<?> cType = (Class<?>) ((ParameterizedType) genericType).getRawType();
			if (Map.class.isAssignableFrom(cType)) {
				responseCast = STRING_MAP;
			} else if (List.class.isAssignableFrom(cType)) {
				Class<?> rType = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
				if (rType != null) {
					if (String.class.isAssignableFrom(rType)) {
						responseCast = STRING_LIST;
					} else if (Long.class.isAssignableFrom(rType)) {
						responseCast = LONG_LIST;
					}
				}
			} else if (Set.class.isAssignableFrom(cType)) {
				Class<?> rType = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
				if (rType != null) {
					if (String.class.isAssignableFrom(rType)) {
						responseCast = STRING_SET;
					}
				}
			}
		} else {
			Class<?> rType = (Class<?>) genericType;
			if (String.class.isAssignableFrom(rType)) {
				responseCast = STRING;
			} else if (Long.class.isAssignableFrom(rType)) {
				responseCast = LONG;
			} else if (Double.class.isAssignableFrom(rType)) {
				responseCast = DOUBLE;
			} else if (Boolean.class.isAssignableFrom(rType)) {
				responseCast = BOOLEAN;
			} else if (Void.class.isAssignableFrom(rType)) {
				responseCast = OK;
			}
		}

		if (responseCast != null) {
			RESPONSE_CAST_MAP.put(genericType, responseCast);
		}
		return responseCast;
	}
}
