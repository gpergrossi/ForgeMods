package com.gpergrossi.util.spacial;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class Large2DArray<T> implements Iterable<T> {

	/** Number of bits per coordinate that fall into a single block */
	private static final int BLOCK_BITS = 3;
	
	/** The block is a square, this is one side of it */
	private static final int BLOCK_SIZE = 1 << BLOCK_BITS;
	
	/** The mask that reduces a coordinate to its in-block coordinate */
	private static final int BLOCK_MASK = BLOCK_SIZE-1;
	
	/**
	 * Stores a small N by N block of values. This helps give locality to entries.
	 * It also creates a two tiered Mapping system that reduces the total size of
	 * the Maps used to store the entries at the expense of more lookup time.
	 */
	private static class StorageBlock implements Iterable<Object> {
		
		private static int subHash(int i, int j) {
			return j*BLOCK_SIZE+i;
		}

		Map<Integer, Object> items;
		
		public StorageBlock() {
			this.items = new HashMap<>();
		}
		
		public Object get(int i, int j) {
			return items.get(subHash(i, j));
		}
		
		public void set(int i, int j, Object value) {
			if (value == null) items.remove(subHash(i, j));
			else items.put(subHash(i, j), value);
		}
		
		private int size() {
			return items.size();
		}

		@Override
		public Iterator<Object> iterator() {
			return items.values().iterator();
		}
	}
	
	/**
	 * Stores the StorageBlocks in a map according to their Hash and Tag
	 */
	private static class BlockCache implements Iterable<StorageBlock> {		
		
		Map<StorageHash, StorageBlock> blocks;
		
		public BlockCache() {
			this.blocks = new HashMap<>();
		}
		
		public StorageBlock getBlock(StorageHash hash) {
			return blocks.get(hash);
		}
		
		public StorageBlock getOrCreateBlock(StorageHash hash) {			
			StorageBlock result = blocks.get(hash);
			if (result == null) {
				result = new StorageBlock();
				blocks.put(hash, result);
			}
			return result;
		}
		
		public void removeBlock(StorageHash hash) {
			blocks.remove(hash);
		}

		@Override
		public Iterator<StorageBlock> iterator() {
			return blocks.values().iterator();
		}
		
	}
	
	public static class StorageHash {
		int hash;
		int tag;
		
		public StorageHash(int i, int j) {
			i = i >>> BLOCK_BITS;
			j = j >>> BLOCK_BITS;
			this.hash = (j << 16) | (i & 0x0000FFFF);
			this.tag = (j & 0xFFFF0000) | (i >>> 16);
		}
		
		@Override
		public int hashCode() {
			return hash;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (((StorageHash) obj).hash != this.hash) return false;
			if (((StorageHash) obj).tag != this.tag) return false;
			return true;
		}
		
		public static String fullHex(int v) {
			String num = ("00000000" + Integer.toHexString(v));
			return "0x" + num.substring(num.length()-8);
		}
		
		@Override
		public String toString() {
			return "StorageHash[hash="+fullHex(hash)+", tag="+fullHex(tag)+"]";
		}
	}
	
	long size;
	BlockCache blockCache = new BlockCache();
	Function<Integer, T[]> arrayAllocator;
	
	public Large2DArray(Function<Integer, T[]> arrayAllocator) {
		this.arrayAllocator = arrayAllocator;
	}	
	
	@SuppressWarnings("unchecked")
	public T get(int i, int j) {
		StorageHash hash = new StorageHash(i, j);
		StorageBlock block = blockCache.getBlock(hash);
		if (block == null) return null;
		
		i = i & BLOCK_MASK;
		j = j & BLOCK_MASK;
		return (T) block.get(i, j);
	}
	
	public void set(int i, int j, T value) {
		StorageHash hash = new StorageHash(i, j);
		StorageBlock block = null;
		if (value == null) block = blockCache.getBlock(hash);
		else block = blockCache.getOrCreateBlock(hash);
		if (block == null) return;
		
		i = i & BLOCK_MASK;
		j = j & BLOCK_MASK;
		
		int originalSize = block.size();
		block.set(i, j, value);
		size += block.size() - originalSize;
		
		if (block.size() == 0) blockCache.removeBlock(hash);
	}

	public long calculateSize() {
		long size = 0;
		for (StorageBlock block : blockCache.blocks.values()) {
			size += block.items.size();
		}
		return size;
	}
	
	public long size() {
		return size;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			Iterator<StorageBlock> blocks = blockCache.iterator();
			Iterator<Object> items = null;
			
			@Override
			public boolean hasNext() {
				if (items != null && items.hasNext()) return true;
				
				while (blocks.hasNext()) {
					items = blocks.next().iterator();
					if (items.hasNext()) return true;
				}
				return false;
			}

			@SuppressWarnings("unchecked")
			@Override
			public T next() {
				if (!hasNext()) throw new NoSuchElementException();
				return (T) items.next();
			}
		};
	}
	
}
