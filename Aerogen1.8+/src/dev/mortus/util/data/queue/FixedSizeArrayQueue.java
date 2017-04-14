package dev.mortus.util.data.queue;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

public class FixedSizeArrayQueue<T extends Comparable<T>> extends AbstractQueue<T> {

	private int writeIndex;
	private int readIndex;
	private int itemsLeft;
	private int modifyCount;
	private final T[] items;
	
	public static <T extends Comparable<T>> FixedSizeArrayQueue<T> consume(T[] items) {
		return new FixedSizeArrayQueue<>(items, true);
	}
	
	private FixedSizeArrayQueue(T[] items, boolean consume) {
		if (consume) {
			this.items = items;
		} else {
			this.items = Arrays.copyOf(items, items.length);
		}
		itemsLeft = items.length;
		writeIndex = 0;
		readIndex = 0;
		modifyCount = 0;
	}
	
	/**
	 * Attempts to add an item to this queue. Since the queue has a fixed capacity,
	 * such an attempt can fail. In such a case, false is returned and the item
	 * is not added.
	 * @param item - item to be added
	 * @return true if the item was added, false if the queue was full
	 */
	@Override
	public synchronized boolean offer(T item) {
		if (itemsLeft == items.length) {
			return false; // Array too full
		}
		
		items[writeIndex] = item;
		
		writeIndex = (writeIndex + 1) % items.length;
		itemsLeft++;
		modifyCount++;
		
		return true;
	}

	@Override
	public synchronized T poll() {
		T item = peek();
		
		readIndex = (readIndex + 1) % items.length;
		itemsLeft--;
		modifyCount++;
		
		return item;
	}

	@Override
	public synchronized T peek() {
		synchronized(this) {
			if (itemsLeft == 0) return null;
			T item = items[readIndex];
			return item;
		}
	}

	@Override
	public synchronized Iterator<T> iterator() {
		return new Iterator<T>() {
			int localReadIndex = FixedSizeArrayQueue.this.readIndex;
			int localItemsLeft = FixedSizeArrayQueue.this.itemsLeft;
			int localModifyCount = FixedSizeArrayQueue.this.modifyCount;
			
			@Override
			public boolean hasNext() {
				if (modifyCount != localModifyCount) throw new ConcurrentModificationException();
				return localItemsLeft > 0;
			}

			@Override
			public T next() {
				if (modifyCount != localModifyCount) throw new ConcurrentModificationException();
				T item = items[localReadIndex];
				
				localReadIndex = (localReadIndex + 1) % items.length;
				localItemsLeft--;
				
				return item;
			}
		};
	}

	@Override
	public synchronized int size() {
		return itemsLeft;
	}
	
}
