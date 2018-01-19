package com.gpergrossi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.gpergrossi.util.data.btree.RedBlackTree;

public class RedBlackTest {

	@Test
	public void testAdd() {
		RedBlackTree<Integer, String> tree = new RedBlackTree<>();
		
		tree.put(0, "Hello");
		tree.put(100, "World");
		tree.put(50, ",");
		tree.put(200, "!");
		
		tree.checkInvariants();
		assertEquals(4, tree.size());
	}
	
	@Test
	public void testGet() {
		RedBlackTree<Integer, String> tree = new RedBlackTree<>();
		
		tree.put(0, "A");
		tree.put(1, "B");
		tree.put(2, "C");
		tree.put(3, "D");
		
		assertEquals("A", tree.get(0));
		assertEquals("B", tree.get(1));
		assertEquals("C", tree.get(2));
		assertEquals("D", tree.get(3));
	}
	
	private <Key, Value> void compare(Map<Key, Value> guide, RedBlackTree<Key, Value> tree) {
		
	}
	
	@Test
	public void testRemove() {
		RedBlackTree<Integer, String> tree = new RedBlackTree<>();
		Map<Integer, String> guide = new TreeMap<>();
		
		for (int i = 0; i < 10; i++) {
			String letter = String.valueOf('A'+i);
			tree.put(i, letter);
			guide.put(i, letter);
		}
		compare(guide, tree);
		

		System.out.println(tree.toString());
		
		tree.remove(0);
		System.out.println(tree.toString());
		
		assertNull(tree.get(0));
		assertEquals("B", tree.get(1));
		assertEquals("C", tree.get(2));
		assertEquals("D", tree.get(4));
		assertEquals("E", tree.get(8));
		assertEquals("F", tree.get(16));
		assertEquals("G", tree.get(32));
		
		tree.remove(16);
		System.out.println(tree.toString());
		
		assertNull(tree.get(0));
		assertEquals("B", tree.get(1));
		assertEquals("C", tree.get(2));
		assertEquals("D", tree.get(4));
		assertEquals("E", tree.get(8));
		assertNull(tree.get(16));
		assertEquals("G", tree.get(32));
		
		tree.remove(4);
		System.out.println(tree.toString());
		
		assertNull(tree.get(0));
		assertEquals("B", tree.get(1));
		assertEquals("C", tree.get(2));
		assertNull(tree.get(4));
		assertEquals("E", tree.get(8));
		assertNull(tree.get(16));
		assertEquals("G", tree.get(32));
	}
	
}
