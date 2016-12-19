package dev.mortus.util.data;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class StableArrayListMapped<T> extends StableArrayList<T> {

	private Map<T, TreeSet<Integer>> indicesMap;
	private TreeSet<Integer> nullIndices;
	
	public StableArrayListMapped(Class<T> clazz, int initialCapacity) {
		super(clazz, initialCapacity);
		this.indicesMap = new HashMap<>();
		this.nullIndices = new TreeSet<>();
	}
	
	@Override
	public int indexOf(Object o) {
		if (isEmpty()) return -1;
		
		if (o == null) {
			Integer index = nullIndices.pollFirst();
			if (index == null) index = -1;
			return index;
		} 
		
		else {
			TreeSet<Integer> list = indicesMap.get(o);
			if (list == null) return -1;
			
			Integer index = list.first();
			if (index == null) return -1;
			
			if (removedIndices.contains(index)) throw new IndexOutOfBoundsException();
			if (!elements[index].equals(o)) throw new IndexOutOfBoundsException();
			return index;
		}
	}
	
	@Override
	public int lastIndexOf(Object o) {
		if (isEmpty()) return -1;
		
		if (o == null) {
			Integer index = nullIndices.pollLast();
			if (index == null) index = -1;
			return index;
		} 
		
		else {
			TreeSet<Integer> list = indicesMap.get(o);
			if (list == null) return -1;
			
			Integer index = list.last(); 
			if (index == null) return -1;
			
			if (removedIndices.contains(index)) throw new IndexOutOfBoundsException();
			if (!elements[index].equals(o)) throw new IndexOutOfBoundsException();
			return index;
		}
	}
	
	@Override
	public T set(int index, T element, boolean grow) {
		T old = super.set(index, element, grow);
		
		TreeSet<Integer> indicesMapList = indicesMap.get(element);
		if (indicesMapList == null) {
			indicesMapList = new TreeSet<>();
			indicesMap.put(element, indicesMapList);
		}
		indicesMapList.add(index);
		
		if (element == null) {
			nullIndices.add(index);
		}
		
		return old;
	}
	
	@Override
	public T remove(int index) {
		T old = super.remove(index);
		
		TreeSet<Integer> indicesMapList = indicesMap.get(old);
		if (indicesMapList != null) {
			indicesMapList.remove(index);
			if (indicesMapList.size() == 0) indicesMap.remove(old);
		}
		
		if (old == null) {
			nullIndices.remove(index);
		}
		
		return old;
	}
	
}
