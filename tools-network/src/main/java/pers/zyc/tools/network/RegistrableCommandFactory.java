package pers.zyc.tools.network;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangyancheng
 */
public class RegistrableCommandFactory implements CommandFactory {

	private final Map<Integer, Constructor<? extends Command>> commandClassMap = new HashMap<>();

	@Override
	public Command createByType(Header header) {
		try {
			return commandClassMap.get(header.getCommandType()).newInstance(header);
		} catch (Exception ignored) {
			return null;
		}
	}

	public void register(int commandType, Class<? extends Command> commandClass) {
		try {
			Constructor<? extends Command> constructor = commandClass.getConstructor(Header.class);
			constructor.newInstance(new Header());
			commandClassMap.put(commandType, constructor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void registerAll(Map<Integer, Class<? extends Command>> commandClassMap) {
		for (Map.Entry<Integer, Class<? extends Command>> entry : commandClassMap.entrySet()) {
			register(entry.getKey(), entry.getValue());
		}
	}
}
