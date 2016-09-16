package dev.mortus.util;

import java.util.Iterator;

public class Pair<T> extends Tuple2<T,T> implements Iterable<T> {

	public Pair(T first, T second) {
		super(first, second);
	}

	/**
	 * Iterates over the non-null elements of this pair.
	 */
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			boolean started = false;
			boolean finished = false;
			
			public boolean hasNext() {
				if (!started) {
					if (first != null) return true;
					started = true;
				}
				if (!finished) {
					if (second != null) return true;
					finished = true;
				}
				return false;
			}
			public T next() {
				if (!started) {
					started = true;
					if (first != null) return first;
				}
				finished = true;
				return second;
			}
		};
	}

}
