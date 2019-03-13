package pers.zyc.tools.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author zhangyancheng
 */
public class SequenceSetTest {

	private static void assertSeq(SequenceSet sequenceSet, int size, long misses, long min, long max, String toString) {
		Assert.assertEquals(size, sequenceSet.size());
		Assert.assertEquals(misses, sequenceSet.misses());
		Assert.assertEquals(min, sequenceSet.min());
		Assert.assertEquals(max, sequenceSet.max());
		Assert.assertEquals(toString, sequenceSet.toString());
	}

	@Test
	public void case_0() {
		SequenceSet sequenceSet = new SequenceSet(20);
		sequenceSet.add("60");
		assertSeq(sequenceSet, 1, 0, 60, 60, "[60]");
		sequenceSet.add("80-100");
		assertSeq(sequenceSet, 1, 0, 60, 100, "[60-100]");
		sequenceSet.add("80-160", "200-260");
		assertSeq(sequenceSet, 2, 1, 60, 260, "[60-160, 200-260]");

		sequenceSet.add("280-300");
		assertSeq(sequenceSet, 2, 1, 60, 300, "[60-160, 200-300]");

		sequenceSet.add("180", "20");
		assertSeq(sequenceSet, 2, 1, 20, 300, "[20, 60-300]");
		sequenceSet.add("40");
		assertSeq(sequenceSet, 1, 0, 20, 300, "[20-300]");
	}

	@Test
	public void case_1() {
		SequenceSet sequenceSet = new SequenceSet(20);
		sequenceSet.add("200", "100", "60");
		assertSeq(sequenceSet, 3, 5, 60, 200, "[60, 100, 200]");
	}

	@Test
	public void case_2() {
		SequenceSet sequenceSet = new SequenceSet(20);
		for (long value = 0; value < 4000; value += 40) {
			sequenceSet.add(value);
		}
		Assert.assertTrue(100 == sequenceSet.size());
		Assert.assertEquals(sequenceSet.size() - 1, sequenceSet.misses());
	}
}
