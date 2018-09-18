package pers.zyc.tools.network;

/**
 * @author zhangyancheng
 */
public interface CommandFactory {

	int HEARTBEAT = 10;

	Command createByType(int commandType);
}
