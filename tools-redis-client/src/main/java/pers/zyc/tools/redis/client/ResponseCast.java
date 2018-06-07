package pers.zyc.tools.redis.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
				return new String((byte[]) response, Protocol.UTF8);
			}

			return response.toString();
		}
	};

	static final ResponseCast<Long> LONG = new ResponseCast<Long>() {

		@Override
		Long cast(Object response) {
			if (response instanceof Long) {
				return (Long) response;
			}

			if (response instanceof byte[]) {
				return Long.parseLong(new String((byte[]) response, Protocol.UTF8));
			}

			throw new RedisClientException("Cannot cast " + String.valueOf(response) + " to Long");
		}
	};

	static final ResponseCast<Boolean> BOOLEAN = new ResponseCast<Boolean>() {

		@Override
		Boolean cast(Object response) {
			if (response instanceof Boolean) {
				return (Boolean) response;
			}

			if (response instanceof String) {
				return stringToBoolean((String) response);
			}

			if (response instanceof byte[]) {
				return stringToBoolean(new String((byte[]) response, Protocol.UTF8));
			}

			throw new RedisClientException("Cannot cast " + String.valueOf(response) + " to Boolean");
		}

		private boolean stringToBoolean(String string) {
			switch (string) {
				case "1":
				case "true":
					return true;
				default:
					return false;
			}
		}
	};

	static final ResponseCast<Double> DOUBLE = new ResponseCast<Double>() {

		@Override
		Double cast(Object response) {
			if (response instanceof Double) {
				return (Double) response;
			}

			if (response instanceof String) {
			}

			return null;
		}
	};

	static final ResponseCast<List<String>> STRING_LIST = new ResponseCast<List<String>>() {

		@Override
		List<String> cast(Object response) {

			return null;
		}
	};

	static final ResponseCast<Set<String>> STRING_SET = new ResponseCast<Set<String>>() {

		@Override
		Set<String> cast(Object response) {
			return null;
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
