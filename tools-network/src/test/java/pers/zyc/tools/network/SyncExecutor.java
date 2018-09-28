package pers.zyc.tools.network;

import java.util.concurrent.Executor;

/**
 * @author zhangyancheng
 */
public class SyncExecutor implements Executor {

	@Override
	public void execute(Runnable command) {
		command.run();
	}

	public static final SyncExecutor INSTANCE = new SyncExecutor();
}
