package pers.zyc.tools.zkclient.listener;

/**
 * @author zhangyancheng
 */
public abstract class ConnectionListenerAdapter implements ConnectionListener {
	@Override
	public void onConnected() {
	}
	
	@Override
	public void onReconnected() {
	}
	
	@Override
	public void onSuspend() {
	}
	
	@Override
	public void onSessionClosed() {
	}
}
