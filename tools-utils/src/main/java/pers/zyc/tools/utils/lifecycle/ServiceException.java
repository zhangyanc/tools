package pers.zyc.tools.utils.lifecycle;

/**
 * @author zhangyancheng
 */
public class ServiceException extends RuntimeException {

	public ServiceException() {
	}

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(Throwable cause) {
		super(cause);
	}

	public static class StartException extends ServiceException {
		public StartException(Throwable cause) {
			super(cause);
		}
	}

	public static class NotRunningException extends ServiceException {

        public NotRunningException(String service) {
            super(service + " not running!");
        }
    }
}
