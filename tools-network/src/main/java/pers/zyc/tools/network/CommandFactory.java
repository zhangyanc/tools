package pers.zyc.tools.network;

/**
 * @author zhangyancheng
 */
public interface CommandFactory {

	int HEARTBEAT = 100;

	Command createByType(int commandType);
}
