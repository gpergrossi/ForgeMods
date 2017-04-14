package dev.mortus.util.data.storage;

import java.util.function.IntFunction;

import dev.mortus.util.data.DoublyLinkedList;

public class GrowingStorage<T extends StorageItem> {

	private IntFunction<T[]> arrayAllocator;
	
	private int index;
	
	private int size;
	private int capacity;
	private int modifyCount;
	
	private DoublyLinkedList<FixedSizeStorage<T>> storages;
	
	public GrowingStorage(IntFunction<T[]> arrayAllocator, int initialCapacity) {
		this.modifyCount = 0;
		this.storages = new DoublyLinkedList<>();
		
		int init = nextPowerOf2(initialCapacity);
		storages.add(new FixedSizeStorage<>(arrayAllocator, init));
		
		this.size = 0;
		this.capacity = init;
	}
	
	public static int nextPowerOf2(int initialCapacity) {
		int npot = initialCapacity;
		npot |= npot >> 1;
		npot |= npot >> 2;
		npot |= npot >> 4;
		npot |= npot >> 8;
		npot |= npot >> 16;
		return npot + 1;
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return (size == 0);
	}

	public boolean contains(T o) {
		if (o == null) return false;
		int index = o.getStorageIndex();
		if (index < 0 || index >= size) return false;
		return (items[index].equals(o));
	}
	
	public boolean add(T e) {
		if (e == null) return false;
		
		items[size] = e;
		e.setStorageIndex(size);
		
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
		
		items[index].setStorageIndex(-1);
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
