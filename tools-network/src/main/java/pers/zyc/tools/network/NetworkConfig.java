package pers.zyc.tools.network;

/**
 * @author zhangyancheng
 */
public class NetworkConfig {

	private boolean tcpNoDelay = true;
	private boolean reuseAddress = true;
	private boolean keepAlive = true;
	private int linger = -1;
	private int sendBuffer = 8 * 1024;
	private int receiveBuffer  = 8 * 1024;

	private int selectors = 0;
	private int requestTimeout = 3000;
	private int maxFrameLength = 4 * 1024 * 1024;

	public boolean isTcpNoDelay() {
		return tcpNoDelay;
	}

	public boolean isReuseAddress() {
		return reuseAddress;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public int getLinger() {
		return linger;
	}

	public int getSendBuffer() {
		return sendBuffer;
	}

	public int getReceiveBuffer() {
		return receiveBuffer;
	}

	public int getSelectors() {
		return selectors;
	}

	public int getRequestTimeout() {
		return requestTimeout;
	}

	public int getMaxFrameLength() {
		return maxFrameLength;
	}
}
