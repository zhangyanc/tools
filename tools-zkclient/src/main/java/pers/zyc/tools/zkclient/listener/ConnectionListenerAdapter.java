package pers.zyc.tools.zkclient.listener;

/**
 * @author zhangyancheng
 */
public abstract class ConnectionListenerAdapter implements ConnectionListener {

	@Override
	public void onConnected(boolean newSession) {
	}

	@Override
	public void onDisconnected(boolean sessionClosed) {
	}
}
