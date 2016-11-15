package dev.mortus.util.data;

public class Tuple2<T,S> {

	public final T first;
	public final S second;
	private final int size;
	
	public Tuple2(T first, S second) {
		this.first = first;
		this.second = second;
		
		if (first != null) {
			if (second != null) size = 2;
			else size = 1;
	    } else {
			if (second != null) size = 1;
			else size = 0;
		}
	}

	/**
	 * @return how many items in the tuple are non-null.
	 */
	public int size() {
		return size;
	}
	
}
