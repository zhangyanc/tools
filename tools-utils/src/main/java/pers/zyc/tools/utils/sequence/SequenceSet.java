package pers.zyc.tools.utils.sequence;

import java.util.Arrays;
import java.util.List;

/**
 * [20, 80-160, 200, 300, 400-500]
 *
 * @author zhangyancheng
 */
public class SequenceSet {
	private Sequence head;

	public boolean add(long value) {
		if (head == null) {
			head = new Sequence(value);
			return true;
		}
		Sequence seq = head;
		while (true) {
			if (seq.contains(value)) {
				return false;
			}
			if (seq.tryMerge(value)) {
				return true;
			}
			if (seq.prev == null && seq.first > value) {
				Sequence newHead = new Sequence(value);
				newHead.next = head;
				head.prev = newHead;
				head = newHead;
				return true;
			}
			if (seq.next == null) {
				Sequence insert = new Sequence(value);
				seq.next = insert;
				insert.prev = seq;
				return true;
			}
			if (seq.next.first > value) {
				Sequence insert = new Sequence(value);
				seq.next.linkNewPrev(insert);
				seq.next = insert;
				insert.prev = seq;
				return true;
			}
			seq = seq.next;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("[");
		Sequence seq = head;
		while (seq != null) {
			builder.append(seq.toString());
			seq = seq.next;
			if (seq != null) {
				builder.append(", ");
			}
		}
		builder.append("]");
		return builder.toString();
	}



	public static void main(String[] args) {
		test1();
	}

	private static void test1() {
		SequenceSet sequenceSet = new SequenceSet();
		sequenceSet.add(80);
		sequenceSet.add(40);
		sequenceSet.add(20);
		System.out.println(sequenceSet);
	}

	private static void test0() {
		List<Long> seqList1 = Arrays.asList(20L, 80L, 160L, 200L, 300L, 400L, 500L);
		List<Long> seqList2 = Arrays.asList(80L, 120L, 140L, 100L, 300L, 460L, 420L, 440L, 480L);

		{
			SequenceSet sequenceSet = new SequenceSet();
			for (long seq : seqList1) {
				sequenceSet.add(seq);
			}
			System.out.println(sequenceSet);

			for (long seq : seqList2) {
				sequenceSet.add(seq);
			}
			System.out.println(sequenceSet);
			sequenceSet.add(380);
			System.out.println(sequenceSet);
		}
	}
}
