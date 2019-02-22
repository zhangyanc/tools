package pers.zyc.tools.utils.sequence;

/**
 * @author zhangyancheng
 */
public class Sequence {
	private static final int SPAN = 20;

	Sequence prev;
	Sequence next;

	long first;
	long last;

	Sequence(long value) {
		first = last = value;
	}

	Sequence(long first, long last) {
		this.first = first;
		this.last = last;
	}

	public boolean contains(long value) {
		return first <= value && value <= last;
	}

	/**
	 * [20, 80-160, 200, 300, 400-500]
	 *
	 * @param sequence
	 */
	public void linkNewPrev(Sequence sequence) {
		if (first - sequence.last == SPAN) {
			sequence.last = last;
			if (next != null) {
				next.prev = sequence;
				sequence.next = next;
			}
		} else {
			prev = sequence;
			sequence.next = this;
		}
	}

	/**
	 * [20, 80-160, 200, 300, 400-500]
	 *
	 * @param value
	 * @return
	 */
	public boolean tryMerge(long value) {
		if (value < first && first - value == SPAN) {
			first = value;
			if (prev != null && prev.last - first == SPAN) {
				prev.last = last;
				prev.next = next;
				next.prev = prev;
			}
			return true;
		}
		if (value > last && value - last == SPAN) {
			last = value;
			if (next != null && next.first - last == SPAN) {
				last = next.last;
				if (next.next == null) {
					next = null;
				} else {
					next.next.prev = this;
					next = next.next;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return first == last ? "" + first : first + "-" + last;
	}
}
