package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.exception.RedisClientException;
import pers.zyc.tools.redis.client.exception.ServerRespondException;
import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.*;

/**
 * @author zhangyancheng
 */
public interface ResponseCast<R> {

	R cast(Object response);

	ResponseCast<Void> OK = new ResponseCast<Void>() {

		@Override
		public Void cast(Object response) {
			if (!"OK".equals(response)) {
				throw new ServerRespondException((String) response);
			}

			return null;
		}
	};

	ResponseCast<String> STRING = new ResponseCast<String>() {

		@Override
		public String cast(Object response) {
			if (response == null) {
				return null;
			}

			if (response instanceof byte[]) {
				return ByteUtil.bytesToString((byte[]) response);
			}

			if (response instanceof String) {
				throw new ServerRespondException((String) response);
			}

			throw new RedisClientException("Cannot cast " + String.valueOf(response) + " to String");
		}
	};

	ResponseCast<Long> LONG = new ResponseCast<Long>() {

		@Override
		public Long cast(Object response) {
			if (response instanceof String) {
				throw new ServerRespondException((String) response);
			}

			if (response instanceof Long) {
				return (Long) response;
			}

			throw new RedisClientException("Cannot cast " + String.valueOf(response) + " to Long");
		}
	};

	ResponseCast<Boolean> BOOLEAN = new ResponseCast<Boolean>() {

		@Override
		public Boolean cast(Object response) {
			return 1 == LONG.cast(response);
		}
	};

	ResponseCast<Double> DOUBLE = new ResponseCast<Double>() {

		@Override
		public Double cast(Object response) {
			if (response == null) {
				return null;
			}

			if (response instanceof byte[]) {
				ByteUtil.byteToDouble((byte[]) response);
			}

			if (response instanceof String) {
				throw new ServerRespondException((String) response);
			}

			throw new RedisClientException("Cannot cast " + String.valueOf(response) + " to Double");
		}
	};

	ResponseCast<List<String>> STRING_LIST = new ResponseCast<List<String>>() {

		@Override
		@SuppressWarnings("unchecked")
		public List<String> cast(Object response) {
			if (response == null) {
				return Collections.emptyList();
			}

			if (response instanceof List) {
				List<byte[]> byteRespList = (List<byte[]>) response;

				List<String> result = new ArrayList<>(byteRespList.size());
				for (byte[] br : byteRespList) {
					result.add(br == null ? null : ByteUtil.bytesToString(br));
				}

				return Collections.unmodifiableList(result);
			}

			throw new RedisClientException("Cannot cast " + String.valueOf(response) + " to List<String>");
		}
	};

	ResponseCast<Set<String>> STRING_SET = new ResponseCast<Set<String>>() {

		@Override
		@SuppressWarnings("unchecked")
		public Set<String> cast(Object response) {
			if (response == null) {
				return Collections.emptySet();
			}

			if (response instanceof List) {
				List<byte[]> byteRespList = (List<byte[]>) response;

				Set<String> result = new HashSet<>(byteRespList.size());
				for (byte[] br : byteRespList) {
					result.add(br == null ? null : ByteUtil.bytesToString(br));
				}

				return Collections.unmodifiableSet(result);
			}

			throw new RedisClientException("Cannot cast " + String.valueOf(response) + " to Set<String>");
		}
	};

	ResponseCast<Map<String, String>> STRING_MAP = new ResponseCast<Map<String, String>>() {

		@Override
		public Map<String, String> cast(Object response) {
			return null;
		}
	};

	ResponseCast<List<Long>> LONG_LIST = new ResponseCast<List<Long>>() {

		@Override
		public List<Long> cast(Object response) {
			return null;
		}
	};
}
