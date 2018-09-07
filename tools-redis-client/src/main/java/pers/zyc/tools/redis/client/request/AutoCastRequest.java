package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.ResponseCast;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static pers.zyc.tools.redis.client.ResponseCast.*;

/**
 * @author zhangyancheng
 */
public abstract class AutoCastRequest<R> extends Request<R> {

	private final ResponseCast<R> cast;

	@SuppressWarnings("unchecked")
	protected AutoCastRequest(byte[]... bulks) {
		super(bulks);

		Type genericType = ((ParameterizedType)
				getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		//通过子类标记的泛型类型获取对应的响应转换器
		cast = (ResponseCast<R>) getCastByteGenericType(genericType);
	}

	public AutoCastRequest(ResponseCast<R> cast, byte[]... bulks) {
		super(bulks);
		this.cast = cast;
	}

	@Override
	public ResponseCast<R> getCast() {
		return cast;
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
