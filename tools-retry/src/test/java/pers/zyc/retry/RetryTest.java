package pers.zyc.retry;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author zhangyancheng
 */
public class RetryTest {

	private void runCallable() throws InterruptedException {
		Thread.sleep((long) (Math.random() * 100));
	}

	/**
	 * 测试Retry Stat
	 */
	@Test
	public void case_RetryStat() throws InterruptedException {
		RetryStat stat = new RetryStat();
		long startTime = stat.getStartTime();

		runCallable();
		Assert.assertTrue(startTime > 0);
		Assert.assertTrue(stat.getAlreadyRetryTimes() == 0);
		Assert.assertTrue(stat.getFirstRetryTime() == 0);
		Assert.assertTrue(stat.getLastRetryTime() == 0);

		stat.retry();
		runCallable();
		long firstRetryTime = stat.getFirstRetryTime();
		Assert.assertTrue(stat.getAlreadyRetryTimes() == 1);
		Assert.assertTrue(firstRetryTime > 0);
		Assert.assertTrue(firstRetryTime == stat.getLastRetryTime());
		Assert.assertTrue(startTime == stat.getStartTime());

		stat.retry();
		runCallable();
		Assert.assertTrue(stat.getAlreadyRetryTimes() == 2);
		Assert.assertTrue(firstRetryTime == stat.getFirstRetryTime());
		//任务执行必需有时间消耗(>1ms), 不然两次充实在同一毫秒将导致firstRetryTime和lastRetryTime相等
		Assert.assertTrue(firstRetryTime != stat.getLastRetryTime());
		Assert.assertTrue(startTime == stat.getStartTime());

		stat.retry();
		runCallable();
		Assert.assertTrue(stat.getAlreadyRetryTimes() == 3);
		Assert.assertTrue(firstRetryTime == stat.getFirstRetryTime());
		//任务执行必需有时间消耗(>1ms), 不然两次充实在同一毫秒将导致firstRetryTime和lastRetryTime相等
		Assert.assertTrue(firstRetryTime != stat.getLastRetryTime());
		Assert.assertTrue(startTime == stat.getStartTime());

		System.out.println(stat);
	}

	/**
	 * 测试BaseRetryPolicy: 固定重试间隔, 无最大重试次数(将无限执行下去, 用外部i控制次数)
	 */
	@Test
	public void case_BaseRetryPolicy_RetryDelay() throws InterruptedException {
		RetryStat stat = new RetryStat();

		long retryDelay = 500;
		BaseRetryPolicy baseRetryPolicy = new BaseRetryPolicy();
		baseRetryPolicy.setRetryDelay(retryDelay);

		int i = 10, j = i;//不设置重试次数将永久执行, 用i控制结束
		long beforeRetry = System.nanoTime();
		while (baseRetryPolicy.awaitToRetry(stat) && i-- > 0) {
			long useTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - beforeRetry);
			//断言正确等待了设置的间隔时间(Thread#sleep本身存在误差, 这里使用20毫秒容错)
			Assert.assertTrue(Math.abs(useTime - retryDelay) < 20);
			beforeRetry = System.nanoTime();
			stat.retry();
		}
		Assert.assertTrue(stat.getAlreadyRetryTimes() == j);
	}

	/**
	 * 测试BaseRetryPolicy: 最大重试次数限制
	 */
	@Test
	public void case_BaseRetryPolicy_MaxRetryTimes() throws InterruptedException {
		RetryStat stat = new RetryStat();
		long retryDelay = 500;
		int maxRetryTimes = 10;
		BaseRetryPolicy baseRetryPolicy = new BaseRetryPolicy();
		baseRetryPolicy.setRetryDelay(retryDelay);
		baseRetryPolicy.setMaxRetryTimes(maxRetryTimes);

		while (baseRetryPolicy.awaitToRetry(stat)) {
			stat.retry();
		}

		Assert.assertTrue(stat.getAlreadyRetryTimes() == maxRetryTimes);
	}
}
