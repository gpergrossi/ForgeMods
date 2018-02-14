package com.gpergrossi.test;

import org.junit.Test;

import com.gpergrossi.util.spacial.Large2DArray;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Random;

public class Large2DArrayTest {

	Random random = new Random();
	int count = 100000;
	int[] coords = new int[count*3];
	
	@Test
	public void testOne() {
		Large2DArray<Integer> l2da = new Large2DArray<>();
		
		l2da.set(40, 40, 70919283);
		assertEquals((int) l2da.get(40, 40), 70919283);
		
	}
	
	@Test
	public void testMany() {
		Large2DArray<Integer> l2da = new Large2DArray<>();
		
		System.out.println("Inserting values");
		for (int i = 0; i < count; i++) {
			int x = random.nextInt();
			int y = random.nextInt();
			int value = random.nextInt();
			
			coords[i*3+0] = x;
			coords[i*3+1] = y;
			coords[i*3+2] = value;
			
			l2da.set(x, y, value);
		}
		
		System.out.println("Verifying values");
		for (int i = 0; i < count; i++) {
			assertEquals((int) l2da.get(coords[i*3+0], coords[i*3+1]), coords[i*3+2]);
		}

		assertEquals(l2da.calculateSize(), l2da.size());
		
		System.out.println("Deleting values");
		for (int i = 0; i < count; i++) {
			assertEquals((int) l2da.get(coords[i*3+0], coords[i*3+1]), coords[i*3+2]);
			l2da.set(coords[i*3+0],	coords[i*3+1], null);
		}
		
		assertEquals(l2da.calculateSize(), l2da.size());
		assertEquals(l2da.size(), 0);

		System.out.println("Confirming deletion");
		for (int i = 0; i < count; i++) {
			assertNull(l2da.get(coords[i*3+0],	coords[i*3+1]));
		}	
	}
	
	@Test
	public void valueUpdate() {
		Large2DArray<Integer> l2da = new Large2DArray<>();
		
		// Create
		l2da.set(40, 40, 70919283);
		assertEquals((int) l2da.get(40, 40), 70919283);
		assertEquals(l2da.size(), 1);

		// Modify
		l2da.set(40, 40, 16234234);
		assertEquals((int) l2da.get(40, 40), 16234234);
		assertEquals(l2da.size(), 1);

		// Delete
		l2da.set(40, 40, null);
		assertNull(l2da.get(40, 40));
		assertEquals(l2da.size(), 0);
	}
	
	
	@Test
	public void redundantDelete() {
		Large2DArray<Integer> l2da = new Large2DArray<>();
		
		l2da.set(40, 40, 70919283);
		assertEquals((int) l2da.get(40, 40), 70919283);
		assertEquals(l2da.size(), 1);
		
		l2da.set(40, 40, null);
		assertNull(l2da.get(40, 40));
		assertEquals(l2da.size(), 0);

		l2da.set(40, 40, null);
		assertNull(l2da.get(40, 40));
		assertEquals(l2da.size(), 0);
	}
	
	@Test
	public void hashCollision() {
		Large2DArray<Integer> l2da = new Large2DArray<>();
		
		Large2DArray.StorageHash hash1 = new Large2DArray.StorageHash(40, 40);
		Large2DArray.StorageHash hash2 = new Large2DArray.StorageHash(40 + 0x80000, 40);
		System.out.println(hash1);
		System.out.println(hash2);
		
		l2da.set(40, 40, 70919283);
		assertEquals((int) l2da.get(40, 40), 70919283);
		assertEquals(l2da.size(), 1);

		l2da.set(40 + 0x80000, 40, 16234234);
		assertEquals((int) l2da.get(40 + 0x80000, 40), 16234234);
		assertEquals(l2da.size(), 2);
		
		l2da.set(40, 40, null);
		assertNull(l2da.get(40, 40));
		assertEquals(l2da.size(), 1);

		l2da.set(40 + 0x80000, 40, null);
		assertNull(l2da.get(40 + 0x80000, 40));
		assertEquals(l2da.size(), 0);
	}
	
}
