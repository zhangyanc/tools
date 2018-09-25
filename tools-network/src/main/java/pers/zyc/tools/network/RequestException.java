package pers.zyc.tools.network;

/**
 * @author zhangyancheng
 */
public class RequestException extends RuntimeException {

	public RequestException() {
	}

	public RequestException(String message) {
		super(message);
	}

	public RequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public RequestException(Throwable cause) {
		super(cause);
	}

	public static class TimeoutException extends RequestException {
	}

	public static class TooMuchRequestException extends RequestException {
	}
}
