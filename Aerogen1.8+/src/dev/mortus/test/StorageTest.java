package dev.mortus.test;

import dev.mortus.util.data.storage.FixedSizeStorage;
import dev.mortus.util.data.storage.GrowingStorage;
import dev.mortus.util.data.storage.StorageItem;

public class StorageTest {

	public static class Item implements StorageItem {

		String str;
		int index = -1;
		
		public Item(String name) {
			this.str = name;
		}
		
		@Override
		public void setStorageIndex(int index) {
			this.index = index;
		}

		@Override
		public int getStorageIndex() {
			return index;
		}
		
	}
	
	public static void main(String[] args) {
		
		fixedSizeTest();
		growingTest();
		
		System.out.println("All tests passed");
		
	}
	
    private static void fixedSizeTest() {
		FixedSizeStorage<Item> storage = new FixedSizeStorage<>(t -> new Item[t], 5);
		
		Item a = new Item("A");
		Item b = new Item("B");
		Item c = new Item("C");
		Item d = new Item("D");
		Item e = new Item("E");
		Item f = new Item("F");
		
		assertTrue(storage.add(a));
		assertTrue(storage.add(b));
		assertTrue(storage.add(c));
		assertTrue(storage.add(d));
		assertTrue(storage.add(e));
		assertFalse(storage.add(f));
		
		assertEquals(a.getStorageIndex(), 0);
		assertEquals(b.getStorageIndex(), 1);
		assertEquals(c.getStorageIndex(), 2);
		assertEquals(d.getStorageIndex(), 3);
		assertEquals(e.getStorageIndex(), 4);
		assertEquals(f.getStorageIndex(), -1);
		
		storage.remove(c);
		storage.add(f);
		
		assertEquals(a.getStorageIndex(), 0);
		assertEquals(b.getStorageIndex(), 1);
		assertEquals(c.getStorageIndex(), -1);
		assertEquals(d.getStorageIndex(), 3);
		assertEquals(e.getStorageIndex(), 2);
		assertEquals(f.getStorageIndex(), 4);
		
		storage.clear();
		assertEquals(storage.size(), 0);
		
		assertFalse(storage.contains(a));
		assertFalse(storage.contains(b));
		assertFalse(storage.contains(c));
		assertFalse(storage.contains(d));
		assertFalse(storage.contains(e));
	}

    private static void growingTest() {
    	
    	int j = 0;
    	for (int i = 1; i < 1024; i *= 2) {
    		for (; j < i; j++) {
    			int npot = GrowingStorage.nextPowerOf2(j);
    	 		assertEquals(npot, i);
    		}
    	}
 	}
    
	private static void assertFalse(boolean cond) {
		if (cond) throw new RuntimeException("Assertion failed");
	}
    
    private static void assertTrue(boolean cond) {
		if (!cond) throw new RuntimeException("Assertion failed");
	}
    
	private static void assertEquals(Object i, Object j) {
		if (!i.equals(j)) throw new RuntimeException("Assertion failed, expected "+j+" got "+i);
	}

	private static void assertEquals(int i, int j) {
		if (i != j) throw new RuntimeException("Assertion failed, expected "+j+" got "+i);
	}
	
}
