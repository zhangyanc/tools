package pers.zyc.tools.network;

/**
 * @author zhangyancheng
 */
public interface CommandFactory {

	/**
	 * 命令解码时通过已解码的命令头获取命令实体
	 *
	 * @param header 命令头
	 * @return 命令实体
	 */
	Command createByType(Header header);
}
