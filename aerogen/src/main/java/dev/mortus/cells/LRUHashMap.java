package dev.mortus.cells;

import java.util.LinkedHashMap;

public class LRUHashMap<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = -5750287256608972491L;

	int size;
	
	public LRUHashMap(int size) {
		super(size, 0.75f, true);
		this.size = size;
	}
	
	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
		return size() > size;
	}
	
}
