package pers.zyc.tools.lifecycle;

/**
 * @author zhangyancheng
 */
public class ServiceException extends RuntimeException {

    public ServiceException() {
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
    }
}
