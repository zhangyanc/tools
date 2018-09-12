package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.request.BaseScan;
import pers.zyc.tools.redis.client.util.Future;
import pers.zyc.tools.redis.client.util.Promise;
import pers.zyc.tools.redis.client.util.ResponsePromise;

import java.util.List;

/**
 * @author zhangyancheng
 */
public abstract class BaseScanner<T> implements Scanner<T> {
	private final ConnectionPool connectionPool;
	private boolean started;
	protected long cursor;

	protected BaseScanner(ConnectionPool connectionPool) {
		this.connectionPool = connectionPool;
	}

	/**
	 * 是否已结束(开始后如果游标为0则表示已scan到结尾)
	 *
	 * @return 是否scan结束
	 */
	public boolean atEnd() {
		return started && cursor == 0;
	}

	Future<T> scan(final BaseScan scan) {
		return new Future<T>() {

			final Promise<ScanResult> promise = sendScan(scan);

			T result;

			@Override
			public T get() {
				List<String> stringList = promise.get().getList();

				synchronized (this) {
					if (result == null) {
						result = parseList(stringList);
					}
				}
				return result;
			}
		};
	}

	private Promise<ScanResult> sendScan(BaseScan scan) {
		return connectionPool.getConnection().send(scan,
				new ResponsePromise<ScanResult>(scan.getCast()) {

					@Override
					protected void onRespond() {
						if (response instanceof Throwable) {
							return;
						}

						started = true;
						cursor = ((ScanResult) this.response).getCursor();
					}
		});
	}

	/**
	 * 转换scan结果列表
	 *
	 * @param stringList scan 原始结果列表
	 * @return 具体scan子类型
	 */
	protected abstract T parseList(List<String> stringList);
}
