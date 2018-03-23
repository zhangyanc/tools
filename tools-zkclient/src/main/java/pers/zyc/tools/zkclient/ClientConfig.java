package pers.zyc.tools.zkclient;

/**
 * @author zhangyancheng
 */
public class ClientConfig {
	private String connectStr = "localhost:2181";
	private int sessionTimeout = 30000;
	private boolean canByReadOnly;
	private boolean syncStart;

	private boolean useRetry = true;
	private int retryTimes = 1;//重试次数
	private int retryPerWaitTimeout = 1000;//重试等待超时

	public String getConnectStr() {
		return connectStr;
	}

	public void setConnectStr(String connectStr) {
		this.connectStr = connectStr;
	}

	public int getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public boolean isCanByReadOnly() {
		return canByReadOnly;
	}

	public void setCanByReadOnly(boolean canByReadOnly) {
		this.canByReadOnly = canByReadOnly;
	}

	public boolean isSyncStart() {
		return syncStart;
	}

	public void setSyncStart(boolean syncStart) {
		this.syncStart = syncStart;
	}

	public boolean isUseRetry() {
		return useRetry;
	}

	public void setUseRetry(boolean useRetry) {
		this.useRetry = useRetry;
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	public int getRetryPerWaitTimeout() {
		return retryPerWaitTimeout;
	}

	public void setRetryPerWaitTimeout(int retryPerWaitTimeout) {
		this.retryPerWaitTimeout = retryPerWaitTimeout;
	}
}
