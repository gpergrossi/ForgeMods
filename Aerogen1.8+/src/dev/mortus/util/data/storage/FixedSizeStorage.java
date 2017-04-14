package dev.mortus.util.data;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.function.IntFunction;

public class FixedSizeStorage<T extends StorageItem> {

	private IntFunction<T[]> arrayAllocator;
	
	private int size;
	private int modifyCount;
	
	private T[] items;
	
	public FixedSizeStorage(IntFunction<T[]> arrayAllocator, int size) {
		this.items = arrayAllocator.apply(size);
		this.size = 0;
		this.modifyCount = 0;
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return (size == 0);
	}

	public Iterator<T> iterator() {
		return new Iterator<T>() {
			int expectedModifyCount = modifyCount;
			int indexOn = 0;
			
			@Override
			public boolean hasNext() {
				if (modifyCount != expectedModifyCount) throw new ConcurrentModificationException();
				return indexOn < size;
			}

			@Override
			public T next() {
				if (modifyCount != expectedModifyCount) throw new ConcurrentModificationException();
				T item = items[indexOn];
				indexOn++;
				return item;
			}
		};
	}

	public T[] toArray() {
		T[] array = arrayAllocator.apply(size());
		Iterator<T> iter = iterator();
		int i = 0;
		while (iter.hasNext()) {
			array[i++] = iter.next();
		}
		return array;
	}

	public boolean add(T e) {
		if (e == null) return false;
		if (size >= items.length) return false;
		
		items[size] = e;
		
		size++;
		modifyCount++;
		
		return true;
	}
	
	public void remove(int index) {
		if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
		
		T replace = null;
		if (size > 1) {
			replace = items[size-1];
			replace.setStorageIndex(index);
			items[size-1] = null;
		}
		
		items[index] = replace;
		
		size--;
		modifyCount++;
	}

	public boolean remove(T o) {
		if (o == null) return false;
		remove(o.getStorageIndex());
		return true;
	}

	public void clear() {
		size = 0;	
		modifyCount++;
	}	
	
}
