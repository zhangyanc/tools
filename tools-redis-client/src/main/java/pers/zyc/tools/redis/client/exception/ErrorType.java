package pers.zyc.tools.redis.client.exception;

/**
 * @author zhangyancheng
 */
public enum ErrorType {
	WARN {

		@Override
		boolean match(String msg) {
			return msg != null && msg.contains("WAR");
		}
	},
	ERROR {

		@Override
		boolean match(String msg) {
			return msg != null && msg.contains("ERR");
		}
	},
	UNKNOWN {

		@Override
		boolean match(String msg) {
			return true;
		}
	};

	abstract boolean match(String msg);

	static ErrorType parse(String errorMsg) {
		for (ErrorType errorType : ErrorType.values()) {
			if (errorType.match(errorMsg)) {
				return errorType;
			}
		}
		throw new RuntimeException("Never happened!");
	}
}
