package pers.zyc.tools.network;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * 可注册命令工厂
 *
 * @author zhangyancheng
 */
public class DefaultCommandFactory implements CommandFactory {

	/**
	 * 命令构造器Map
	 */
	private final Map<Integer, Constructor<? extends Command>> commandConstructorMap = new HashMap<>();

	@Override
	public Command createByHeader(Header header) {
		try {
			Constructor<? extends Command> constructor = commandConstructorMap.get(header.getCommandType());
			if (constructor != null) {
				return constructor.newInstance(header);
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	/**
	 * 添加命令，命令类必须提供单独入参为Header对象的构造方法
	 *
	 * @param commandType 命令类型
	 * @param commandClass 命令类
	 */
	public void addCommand(Class<? extends Command> commandClass, int commandType) {
		try {
			Constructor<? extends Command> constructor = commandClass.getConstructor(Header.class);
			constructor.newInstance(new Header());
			commandConstructorMap.put(commandType, constructor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
