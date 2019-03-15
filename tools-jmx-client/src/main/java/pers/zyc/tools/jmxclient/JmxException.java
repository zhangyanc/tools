package pers.zyc.tools.jmxclient;

/**
 * @author zhangyancheng
 */
public class JmxException extends RuntimeException {

	public JmxException(String message) {
		super(message);
	}

	public JmxException(String message, Throwable cause) {
		super(message, cause);
	}

	public JmxException(Throwable cause) {
		super(cause);
	}
}
