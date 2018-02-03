package com.gpergrossi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import com.gpergrossi.util.data.Tuple2;
import com.gpergrossi.util.data.WeightedList;

public class WeightedListTest {

	@Test
	public void testConstructor() {
		WeightedList<String> list = new WeightedList<>();

		assertTrue(list.isEmpty());
		assertEquals(0, list.size());
		assertEquals(0, list.getTotalWeight());
		
		assertEquals(0, list.getWeight("not in tree"));
	}
	
	@Test
	public void testRemoveEmpty() {
		Random random = new Random();
		WeightedList<String> list = new WeightedList<>();
		
		assertNull(list.getRandom(random));

		// Assert no errors:
		list.clear();
		list.remove("not in tree");
		list.removeRandom(random);
	}

	@Test
	public void testAdd() {
		WeightedList<String> list = new WeightedList<>();
		
		list.add("Red", 1);
		list.add("Yellow", 2);
		list.add("Green", 4);
		list.add("Blue", 8);
		
		assertFalse(list.isEmpty());
		assertEquals(4, list.size());
		assertEquals(15, list.getTotalWeight());
		
		assertEquals(1, list.getWeight("Red"));
		assertEquals(2, list.getWeight("Yellow"));
		assertEquals(4, list.getWeight("Green"));
		assertEquals(8, list.getWeight("Blue"));
	}
	
	@Test
	public void testAddOverlap() {
		WeightedList<String> list = new WeightedList<>();
		
		list.add("Red", 1);
		list.add("Blue", 2);
		list.add("Red", 4);
		list.add("Blue", 8);

		assertFalse(list.isEmpty());
		assertEquals(2, list.size());
		assertEquals(15, list.getTotalWeight());
		
		assertEquals(5, list.getWeight("Red"));
		assertEquals(10, list.getWeight("Blue"));
	}
	
	@Test
	public void testRemove() {
		WeightedList<String> list = new WeightedList<>();
		
		list.add("Red", 1);
		list.add("Yellow", 2);
		list.add("Green", 4);
		list.add("Blue", 8);

		list.remove("Yellow");
		assertFalse(list.isEmpty());
		assertEquals(3, list.size());
		assertEquals(13, list.getTotalWeight());
		
		list.remove("Blue");
		assertFalse(list.isEmpty());
		assertEquals(2, list.size());
		assertEquals(5, list.getTotalWeight());
		
		list.remove("Red");
		assertFalse(list.isEmpty());
		assertEquals(1, list.size());
		assertEquals(4, list.getTotalWeight());
		
		list.remove("Green");
		assertTrue(list.isEmpty());
		assertEquals(0, list.size());
		assertEquals(0, list.getTotalWeight());
	}
	
	@Test
	public void testGetRandom() {
		WeightedList<String> list = new WeightedList<>();
		Random random = new Random();
		
		list.add("Heads", 1);
		list.add("Tails", 1);

		for (int i = 0; i < 20; i++) {
			String roll = list.getRandom(random);
			assertNotNull(roll);
			assertTrue(roll.equals("Heads") || roll.equals("Tails"));
		}
	}
	
	@Test
	public void testRemoveRandom() {
		WeightedList<String> list = new WeightedList<>();
		Random random = new Random();
		
		list.add("Red", 1);
		list.add("Yellow", 2);
		list.add("Green", 4);
		list.add("Blue", 8);

		int expectedSize = 4;
		int expectedWeight = 15;
		
		for (int i = 0; i < 16; i++) {
			Tuple2<String, Integer> roll = list.removeRandom(random);
			
			if (roll.first != null) expectedSize--;
			expectedWeight -= roll.second;
			
			assertEquals(expectedSize, list.size());
			assertEquals(expectedWeight, list.getTotalWeight());
			if (i < 4) assertNotNull(roll.first);
			else assertNull(roll.first);
		}
	}
	
}
