package pers.zyc.tools.network;

/**
 * @author zhangyancheng
 */
public class NetworkException extends RuntimeException {

	public NetworkException() {
	}

	public NetworkException(String message) {
		super(message);
	}

	public NetworkException(String message, Throwable cause) {
		super(message, cause);
	}

	public NetworkException(Throwable cause) {
		super(cause);
	}

	public static class TimeoutException extends NetworkException {
	}

	public static class TooMuchRequestException extends NetworkException {
	}
}
