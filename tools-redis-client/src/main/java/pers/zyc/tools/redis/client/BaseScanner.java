package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.request.BaseScan;
import pers.zyc.tools.redis.client.request.key.Scan;
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

	Future<T> scan(BaseScan scan) {
		final Promise<Scan.ScanResult> promise =  connectionPool.getConnection().send(scan,
				new ResponsePromise<BaseScan.ScanResult>(scan.getCast()) {

					@Override
					protected void onRespond() {
						if (response instanceof Throwable) {
							return;
						}

						BaseScan.ScanResult scanResult = (BaseScan.ScanResult) this.response;
						started = true;
						cursor = scanResult.key();
					}
		});

		return new Future<T>() {

			T result;

			@Override
			public T get() {
				List<String> stringList = promise.get().value();

				synchronized (this) {
					if (result == null) {
						result = parseList(stringList);
					}
				}
				return result;
			}
		};
	}

	/**
	 * 转换scan结果列表
	 *
	 * @param stringList scan 原始结果列表
	 * @return 具体scan子类型
	 */
	protected abstract T parseList(List<String> stringList);
}
