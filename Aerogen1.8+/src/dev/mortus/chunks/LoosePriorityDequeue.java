package dev.mortus.chunks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;

public class LoosePriorityDequeue<T extends Object> extends ArrayList<T> implements Queue<T> {

	private static final long serialVersionUID = 6955654743124598514L;
	
	T currentNextBest;
	Comparator<T> comparator;
	Random random;
	int searchIters = 4;
	
	public LoosePriorityDequeue(int initialCapacity) {
		this(initialCapacity, null);
	}
	
	public LoosePriorityDequeue(int initialCapacity, Comparator<T> comparator) {
		super(initialCapacity);
		this.comparator = comparator;
		this.random = new Random();
	}
	
	private int compare(T o1, T o2) {
		if (o1 == null) return 1;
		if (o2 == null) return -1;
		if (comparator != null) return comparator.compare(o1, o2);
		return 0;
	}

	public boolean offer(T item) {
		super.add(item);
		if (compare(item, currentNextBest) < 0) {
			currentNextBest = item;
		}
		return true;
	}

	public T remove() {
		T head = poll();
        if (head != null)
            return head;
        else
            throw new NoSuchElementException();
	}
	
	public T element() {
		T head = peek();
        if (head != null)
            return head;
        else
            throw new NoSuchElementException();
	}

	public T poll() {
		T head = peek();
		if (head == currentNextBest) {
			currentNextBest = null;
		}
		remove(head);
		return head;
	}

	public T peek() {
		if (this.isEmpty()) return null;
		
		T localBest = randomElement();
		
		if (!localBest.equals(currentNextBest)) {
			if (compare(currentNextBest, localBest) < 0) {
				T swap = currentNextBest;
				currentNextBest = localBest;
				localBest = swap;
			}
		}

		for (int i = 0; i < searchIters; i++) {
			T element = randomElement();
			if (element.equals(localBest))continue;
			if (compare(element, localBest) < 0) {
				currentNextBest = localBest;
				localBest = element;
			}
		}
		
		return localBest;
	}

	private T randomElement() {
		int index = random.nextInt(this.size());
		return this.get(index);
	}
	
}
