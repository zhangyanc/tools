package pers.zyc.tools.network;

/**
 * @author zhangyancheng
 */
public interface Commands {

	int HEARTBEAT = 100;

	int ECHO = 10;

	int ECHO_ACK = 11;

	int DAYTIME = 12;

	int DAYTIME_ACK = 13;
}
