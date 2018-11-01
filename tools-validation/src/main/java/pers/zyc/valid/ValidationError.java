package pers.zyc.valid;

public class ValidationError extends Exception {
    private String errorCode;
    private Object[] errorArgs;
    private String defaultMessage;

    public ValidationError(String errorCode) {
        this.errorCode = errorCode;
    }

    public ValidationError(String errorCode, String defaultMessage) {
        this.errorCode = errorCode;
        this.defaultMessage = defaultMessage;
    }

    public ValidationError(String errorCode, Object[] errorArgs, String defaultMessage) {
        this.errorCode = errorCode;
        this.errorArgs = errorArgs;
        this.defaultMessage = defaultMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object[] getErrorArgs() {
        return errorArgs;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}