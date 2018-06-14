package pers.zyc.tools.redis.client;

import java.util.*;

/**
 * @author zhangyancheng
 */
abstract class ResponseCast<R> {

	abstract R cast(Object response);

	static final ResponseCast<String> STRING = new ResponseCast<String>() {

		@Override
		String cast(Object response) {
			if (response == null) {
				return null;
			}

			if (response instanceof String) {
				return (String) response;
			}

			if (response instanceof byte[]) {
				return Util.bytesToString((byte[]) response);
			}

			throw new RedisClientException("Cannot cast " + String.valueOf(response) + " to String");
		}
	};

	static final ResponseCast<Long> LONG = new ResponseCast<Long>() {

		@Override
		Long cast(Object response) {
			if (response instanceof String) {
				throw new RedisClientException((String) response);
			}

			if (response instanceof Long) {
				return (Long) response;
			}

			throw new RedisClientException("Cannot cast " + String.valueOf(response) + " to Long");
		}
	};

	static final ResponseCast<Boolean> BOOLEAN = new ResponseCast<Boolean>() {

		@Override
		Boolean cast(Object response) {
			if (response instanceof byte[]) {
				return Util.bytesToLong((byte[]) response) == 1;
			}

			throw new RedisClientException("Cannot cast " + String.valueOf(response) + " to Boolean");
		}
	};

	static final ResponseCast<Double> DOUBLE = new ResponseCast<Double>() {

		@Override
		Double cast(Object response) {
			if (response == null) {
				return null;
			}

			if (response instanceof byte[]) {
				Util.byteToDouble((byte[]) response);
			}

			throw new RedisClientException("Cannot cast " + String.valueOf(response) + " to Double");
		}
	};

	static final ResponseCast<List<String>> STRING_LIST = new ResponseCast<List<String>>() {

		@Override
		@SuppressWarnings("unchecked")
		List<String> cast(Object response) {
			if (response == null) {
				return Collections.emptyList();
			}

			if (response instanceof List) {
				List<byte[]> byteRespList = (List<byte[]>) response;

				List<String> result = new ArrayList<>(byteRespList.size());
				for (byte[] br : byteRespList) {
					result.add(br == null ? null : Util.bytesToString(br));
				}

				return Collections.unmodifiableList(result);
			}

			throw new RedisClientException("Cannot cast " + String.valueOf(response) + " to List<String>");
		}
	};

	static final ResponseCast<Set<String>> STRING_SET = new ResponseCast<Set<String>>() {

		@Override
		@SuppressWarnings("unchecked")
		Set<String> cast(Object response) {
			if (response == null) {
				return Collections.emptySet();
			}

			if (response instanceof List) {
				List<byte[]> byteRespList = (List<byte[]>) response;

				Set<String> result = new HashSet<>(byteRespList.size());
				for (byte[] br : byteRespList) {
					result.add(br == null ? null : Util.bytesToString(br));
				}

				return Collections.unmodifiableSet(result);
			}

			throw new RedisClientException("Cannot cast " + String.valueOf(response) + " to Set<String>");
		}
	};

	static final ResponseCast<Map<String, String>> STRING_MAP = new ResponseCast<Map<String, String>>() {

		@Override
		Map<String, String> cast(Object response) {
			return null;
		}
	};

	static final ResponseCast<List<Long>> LONG_LIST = new ResponseCast<List<Long>>() {

		@Override
		List<Long> cast(Object response) {
			return null;
		}
	};
}
