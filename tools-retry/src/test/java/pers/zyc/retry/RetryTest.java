package pers.zyc.retry;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
	 * 测试BaseRetryPolicy: 异常处理
	 */
	@Test
	public void case_BaseRetryPolicy_ExceptionHandler() {
		Callable<Object> callable = new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				return new Object();
			}
		};

		BaseRetryPolicy baseRetryPolicy = new BaseRetryPolicy();
		//非ExceptionHandler callable则总是返回true
		Assert.assertTrue(baseRetryPolicy.handleException(new Exception(), callable));

		class ExceptionHandledCallable implements Callable<Object>, RetryExceptionHandler {

			@Override
			public Object call() throws Exception {
				return new Object();
			}

			@Override
			public Boolean handleException(Throwable cause, Callable<?> callable) {
				return cause instanceof RuntimeException;
			}
		}
		callable = new ExceptionHandledCallable();
		//返回ExceptionHandler callable的处理结果
		Assert.assertFalse(baseRetryPolicy.handleException(new Exception(), callable));
		Assert.assertTrue(baseRetryPolicy.handleException(new NullPointerException(), callable));
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

	/**
	 * 测试RetryLoop: 不设置重试策略, 不进行重试
	 */
	@Test
	public void case_RetryLoop_DoNotRetry() throws InterruptedException {
		final Exception testException = new Exception();
		try {
			RetryLoop.execute(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					throw testException;
				}
			}, null);
		} catch (RetryFailedException e) {
			Assert.assertTrue(testException == e.getCause());
			Assert.assertTrue(0 == e.getRetryStat().getAlreadyRetryTimes());
		}
	}

	/**
	 * 测试RetryLoop: 重试策略的异常处理
	 */
	@Test
	public void case_RetryLoop_SpecificRetryPolicy() throws InterruptedException {
		final AtomicReference<Exception> exceptionHolder = new AtomicReference<>();
		final Exception testException = new Exception();
		exceptionHolder.set(testException);

		final AtomicInteger exeTimes = new AtomicInteger(0);
		final Callable<Object> callable = new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				exeTimes.getAndIncrement();
				throw exceptionHolder.get();
			}
		};

		final int maxRetryTimes = 3;
		final RetryPolicy retryPolicy = new BaseRetryPolicy() {
			@Override
			public Boolean handleException(Throwable cause, Callable<?> callable) {
				//只对testException可继续重试
				return cause == testException;
			}

			{
				setMaxRetryTimes(maxRetryTimes);
				setRetryDelay(200);
			}
		};

		try {
			RetryLoop.execute(callable, retryPolicy);
			//call总是抛出异常, 不会重试成功
			Assert.fail();
		} catch (RetryFailedException e) {
			Assert.assertTrue(testException == e.getCause());
			Assert.assertTrue(maxRetryTimes == e.getRetryStat().getAlreadyRetryTimes());
			//重试次数比执行次数少1
			Assert.assertTrue(maxRetryTimes == (exeTimes.get() - 1));
		}

		Exception newException = new Exception();
		exceptionHolder.set(newException);//非可重试异常, 将不会重试
		exeTimes.set(0);

		try {
			RetryLoop.execute(callable, retryPolicy);
			Assert.fail();
		} catch (RetryFailedException e) {
			Assert.assertTrue(newException == e.getCause());
			Assert.assertTrue(0 == e.getRetryStat().getAlreadyRetryTimes());
			Assert.assertTrue(0 == (exeTimes.get() - 1));
		}
	}

	/**
	 * 测试RetryLoop: 重试任务执行成功
	 */
	@Test
	public void case_RetryLoop_CallSuccess() throws InterruptedException {
		final AtomicInteger exeTimes = new AtomicInteger(0);
		final AtomicInteger whenCallSuccess = new AtomicInteger(1);

		final Object result = new Object();
		final Callable<Object> callable = new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				if (exeTimes.incrementAndGet() == whenCallSuccess.get()) {
					return result;
				}
				throw new Exception();
			}
		};
		RetryPolicy retryPolicy = new BaseRetryPolicy() {
			{
				setRetryDelay(500);
				setMaxRetryTimes(3);
			}
		};

		try {
			Assert.assertTrue(result == RetryLoop.execute(callable, retryPolicy));
			Assert.assertTrue(exeTimes.get() == whenCallSuccess.get());
		} catch (RetryFailedException e) {
			Assert.fail();
		}

		exeTimes.set(0);
		whenCallSuccess.set(3);

		try {
			Assert.assertTrue(result == RetryLoop.execute(callable, retryPolicy));
			Assert.assertTrue(exeTimes.get() == whenCallSuccess.get());
		} catch (RetryFailedException e) {
			Assert.fail();
		}

		exeTimes.set(0);
		whenCallSuccess.set(5);

		try {
			RetryLoop.execute(callable, retryPolicy);
			Assert.fail();
		} catch (RetryFailedException e) {
			Assert.assertTrue(e.getRetryStat().getAlreadyRetryTimes() == (exeTimes.get() - 1));
		}
	}
}
