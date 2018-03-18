package pers.zyc.retry;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

		Assert.assertTrue(maxRetryTimes == stat.getAlreadyRetryTimes());
	}

	/**
	 * 测试BaseRetryPolicy: 递增重试间隔
	 */
	@Test
	public void case_BaseRetryPolicy_UseExp() throws InterruptedException {
		RetryStat stat = new RetryStat();
		long retryDelay = 1000;
		int maxRetryTimes = 4;

		BaseRetryPolicy baseRetryPolicy = new BaseRetryPolicy();
		baseRetryPolicy.setRetryDelay(retryDelay);
		baseRetryPolicy.setMaxRetryTimes(maxRetryTimes);
		baseRetryPolicy.setUseExp(true);
		baseRetryPolicy.setBaseNum(2.0);

		Queue<Long> awaitTimes = new LinkedList<Long>() {
			{
				add(1000L);
				add(2000L);
				add(4000L);
				add(8000L);
			}
		};

		long beforeRetry = System.nanoTime();
		while (baseRetryPolicy.awaitToRetry(stat)) {
			long useTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - beforeRetry);
			//断言正确等待了设置的间隔时间(Thread#sleep本身存在误差, 这里使用20毫秒容错)
			Assert.assertTrue(Math.abs(useTime - awaitTimes.remove()) < 20);
			stat.retry();
			beforeRetry = System.nanoTime();
		}

		Assert.assertTrue(maxRetryTimes == stat.getAlreadyRetryTimes());
	}

	/**
	 * 测试BaseRetryPolicy: 最大重试延迟
	 */
	@Test
	public void case_BaseRetryPolicy_MaxRetryDelay() throws InterruptedException {
		RetryStat stat = new RetryStat();
		long retryDelay = 1000;
		long maxRetryDelay = 3000;
		int maxRetryTimes = 4;

		BaseRetryPolicy baseRetryPolicy = new BaseRetryPolicy();
		baseRetryPolicy.setRetryDelay(retryDelay);
		baseRetryPolicy.setMaxRetryTimes(maxRetryTimes);
		baseRetryPolicy.setMaxRetryDelay(maxRetryDelay);
		baseRetryPolicy.setUseExp(true);
		baseRetryPolicy.setBaseNum(2.0);

		Queue<Long> awaitTimes = new LinkedList<Long>() {
			{
				add(1000L);
				add(2000L);
				add(3000L);
				add(3000L);
			}
		};

		long beforeRetry = System.nanoTime();
		while (baseRetryPolicy.awaitToRetry(stat)) {
			long useTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - beforeRetry);
			//断言正确等待了设置的间隔时间(Thread#sleep本身存在误差, 这里使用20毫秒容错)
			Assert.assertTrue(Math.abs(useTime - awaitTimes.remove()) < 20);
			beforeRetry = System.nanoTime();
			stat.retry();
		}

		Assert.assertTrue(maxRetryTimes == stat.getAlreadyRetryTimes());
	}

	/**
	 * 测试ConditionalRetryPolicy: 条件检查不通过将不进行重试
	 */
	@Test
	public void case_ConditionalRetryPolicy_Condition_False() throws InterruptedException {
		RetryStat stat = new RetryStat();
		final AtomicBoolean condition = new AtomicBoolean(false);
		RetryCondition retryCondition = new RetryCondition() {
			@Override
			public boolean check() {
				//always false
				return condition.get();
			}

			@Override
			public Object getMutex() {
				return this;
			}
		};

		ConditionalRetryPolicy conditionalRetryPolicy = new ConditionalRetryPolicy(retryCondition);
		conditionalRetryPolicy.setRetryDelay(500);

		while (conditionalRetryPolicy.awaitToRetry(stat)) {
			stat.retry();
		}

		Assert.assertTrue(0 == stat.getAlreadyRetryTimes());
	}

	/**
	 * 测试ConditionalRetryPolicy: 重试条件提前满足可唤醒重试等待线程
	 */
	@Test
	public void case_ConditionalRetryPolicy_Condition_Advance() throws InterruptedException {
		RetryStat stat = new RetryStat();
		final AtomicBoolean condition = new AtomicBoolean(false);
		final RetryCondition retryCondition = new RetryCondition() {
			@Override
			public boolean check() {
				//always false
				return condition.get();
			}

			@Override
			public Object getMutex() {
				return this;
			}
		};

		ConditionalRetryPolicy conditionalRetryPolicy = new ConditionalRetryPolicy(retryCondition);
		conditionalRetryPolicy.setRetryDelay(5000);
		int maxRetryTimes = 3;
		conditionalRetryPolicy.setMaxRetryTimes(maxRetryTimes);

		final long advanceTime = 3000;
		new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						sleep(advanceTime);
					} catch (InterruptedException e) {
						return;
					}

					synchronized (retryCondition) {
						condition.set(true);
						retryCondition.notify();
					}
				}
			}
		}.start();

		long beforeRetry = System.nanoTime();
		while (conditionalRetryPolicy.awaitToRetry(stat)) {
			long useTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - beforeRetry);
			//断言正确等待了设置的间隔时间(Thread#sleep本身存在误差, 这里使用20毫秒容错)
			Assert.assertTrue(Math.abs(useTime - advanceTime) < 20);
			beforeRetry = System.nanoTime();
			condition.set(false);
			stat.retry();
		}

		Assert.assertTrue(maxRetryTimes == stat.getAlreadyRetryTimes());
	}
}
