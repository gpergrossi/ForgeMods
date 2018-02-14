package com.gpergrossi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.gpergrossi.util.geom.ranges.Int1DMultiRange;
import com.gpergrossi.util.geom.ranges.Int1DRange;

public class Int1DMultiRangeTest {

	@Test
	public void testAddNoOverlap() {
		Int1DMultiRange multirange = new Int1DMultiRange();
		multirange.addRange(0, 5);
		multirange.addRange(10, 15);
		multirange.addRange(20, 25);
		
		assertEquals(18, multirange.size());
		assertEquals(3, multirange.getRanges().size());
	}

	@Test
	public void testAddOverlapAscending() {
		Int1DMultiRange multirange = new Int1DMultiRange();
		multirange.addRange(0, 15);
		multirange.addRange(10, 25);
		multirange.addRange(20, 35);
		
		assertEquals(36, multirange.size());
		assertEquals(1, multirange.getRanges().size());
	}
	
	@Test
	public void testAddAdjacentAscending() {
		Int1DMultiRange multirange = new Int1DMultiRange();
		multirange.addRange(0, 9);
		multirange.addRange(10, 19);
		multirange.addRange(20, 29);
		
		assertEquals(30, multirange.size());
		assertEquals(1, multirange.getRanges().size());
	}

	@Test
	public void testAddOverlapDescending() {
		Int1DMultiRange multirange = new Int1DMultiRange();
		multirange.addRange(20, 35);
		multirange.addRange(10, 25);
		multirange.addRange(0, 15);
		
		assertEquals(36, multirange.size());
		assertEquals(1, multirange.getRanges().size());
	}
	
	@Test
	public void testAddAdjacentDescending() {
		Int1DMultiRange multirange = new Int1DMultiRange();
		multirange.addRange(20, 29);
		multirange.addRange(10, 19);
		multirange.addRange(0, 9);
		
		assertEquals(30, multirange.size());
		assertEquals(1, multirange.getRanges().size());
	}
	
	@Test
	public void testAddOverlapMultiple() {
		Int1DMultiRange multirange = new Int1DMultiRange();
		multirange.addRange(0, 5);
		multirange.addRange(10, 15);
		multirange.addRange(20, 25);
		multirange.addRange(-5, 30);
		
		assertEquals(36, multirange.size());
		assertEquals(1, multirange.getRanges().size());
	}
	
	@Test
	public void testContains() {
		Int1DMultiRange multirange = new Int1DMultiRange();
		multirange.addRange(0, 3);
		multirange.addRange(5, 7);

		assertFalse(multirange.contains(-1));
		assertTrue(multirange.contains(0));
		assertTrue(multirange.contains(1));
		assertTrue(multirange.contains(2));
		assertTrue(multirange.contains(3));
		assertFalse(multirange.contains(4));
		assertTrue(multirange.contains(5));
		assertTrue(multirange.contains(6));
		assertTrue(multirange.contains(7));
		assertFalse(multirange.contains(8));
	}
	
	@Test
	public void testRemoveExact() {
		Int1DMultiRange multirange = new Int1DMultiRange();
		multirange.addRange(0, 10);
		multirange.removeRange(0, 10);
		
		assertEquals(0, multirange.size());
		assertEquals(0, multirange.getRanges().size());
	}
	
	@Test
	public void testRemoveOverlapLowSingle() {
		Int1DMultiRange multirange = new Int1DMultiRange();
		multirange.addRange(2, 8);
		multirange.removeRange(0, 5);
		
		assertEquals(3, multirange.size());
		assertEquals(1, multirange.getRanges().size());
		assertFalse(multirange.contains(5));
		assertTrue(multirange.contains(6));
		assertTrue(multirange.contains(7));
		assertTrue(multirange.contains(8));
		assertFalse(multirange.contains(9));
	}
	
	@Test
	public void testRemoveOverlapHighSingle() {
		Int1DMultiRange multirange = new Int1DMultiRange();
		multirange.addRange(2, 8);
		multirange.removeRange(5, 10);
		
		assertEquals(3, multirange.size());
		assertEquals(1, multirange.getRanges().size());
		assertFalse(multirange.contains(1));
		assertTrue(multirange.contains(2));
		assertTrue(multirange.contains(3));
		assertTrue(multirange.contains(4));
		assertFalse(multirange.contains(5));
	}
	
	@Test
	public void testRemoveOverlapLowAndHighSingle() {
		Int1DMultiRange multirange = new Int1DMultiRange();
		multirange.addRange(2, 8);
		multirange.removeRange(0, 10);
		
		assertEquals(0, multirange.size());
		assertEquals(0, multirange.getRanges().size());
	}
	
	@Test
	public void testRemoveUnderlapSingle() {
		Int1DMultiRange multirange = new Int1DMultiRange();
		multirange.addRange(0, 5);
		multirange.removeRange(2, 3);
		
		assertEquals(4, multirange.size());
		assertEquals(2, multirange.getRanges().size());
		assertFalse(multirange.contains(-1));
		assertTrue(multirange.contains(0));
		assertTrue(multirange.contains(1));
		assertFalse(multirange.contains(2));
		assertFalse(multirange.contains(3));
		assertTrue(multirange.contains(4));
		assertTrue(multirange.contains(5));
		assertFalse(multirange.contains(6));
	}
	
	@Test
	public void testRemoveOverlapDouble() {
		Int1DMultiRange multirange = new Int1DMultiRange();
		multirange.addRange(0, 2);
		multirange.addRange(4, 6);
		multirange.removeRange(1, 4);
		
		assertEquals(3, multirange.size());
		assertEquals(2, multirange.getRanges().size());
		assertFalse(multirange.contains(-1));
		assertTrue(multirange.contains(0));
		assertFalse(multirange.contains(1));
		assertFalse(multirange.contains(2));
		assertFalse(multirange.contains(3));
		assertFalse(multirange.contains(4));
		assertTrue(multirange.contains(5));
		assertTrue(multirange.contains(6));
		assertFalse(multirange.contains(7));
	}
	
	@Test
	public void testInvert() {		
		Int1DMultiRange multirange = new Int1DMultiRange();
		multirange.addRange(0, 0);
		
		assertEquals(1, multirange.size());
		assertEquals(1, multirange.getRanges().size());
		assertFalse(multirange.contains(-1));
		assertTrue(multirange.contains(0));
		assertFalse(multirange.contains(1));

		multirange = multirange.compliment();
		
		long size = Int1DRange.ALL.size() - 1L;
		assertEquals(size, multirange.size());
		assertEquals(2, multirange.getRanges().size());
		assertTrue(multirange.contains(-1));
		assertFalse(multirange.contains(0));
		assertTrue(multirange.contains(1));
	}
	
	@Test
	public void testUnion() {		
		Int1DMultiRange multirangeA = new Int1DMultiRange();
		multirangeA.addRange(0, 0);
		multirangeA.addRange(2, 2);
		multirangeA.addRange(4, 4);
		multirangeA.addRange(7, 7);
		
		Int1DMultiRange multirangeB = new Int1DMultiRange();
		multirangeB.addRange(1, 1);
		multirangeB.addRange(3, 3);
		multirangeB.addRange(5, 5);
		multirangeB.addRange(8, 8);
		
		Int1DMultiRange multirange = Int1DMultiRange.union(multirangeA, multirangeB);
		
		assertEquals(8, multirange.size());
		assertEquals(2, multirange.getRanges().size());
		assertFalse(multirange.contains(-1));
		assertTrue(multirange.contains(0));
		assertTrue(multirange.contains(1));
		assertTrue(multirange.contains(2));
		assertTrue(multirange.contains(3));
		assertTrue(multirange.contains(4));
		assertTrue(multirange.contains(5));
		assertFalse(multirange.contains(6));
		assertTrue(multirange.contains(7));
		assertTrue(multirange.contains(8));
		assertFalse(multirange.contains(9));
	}
	
	@Test
	public void testIntersect() {		
		Int1DMultiRange multirangeA = new Int1DMultiRange();
		multirangeA.addRange(0, 3);
		multirangeA.addRange(5, 10);
		// 0-3, 5-10
		
		Int1DMultiRange multirangeB = new Int1DMultiRange();
		multirangeB.addRange(2, 6);
		multirangeB.addRange(9, 10);
		// 2-6, 9-10
		
		Int1DMultiRange multirange = Int1DMultiRange.intersection(multirangeA, multirangeB);
		// 2-3, 5-6, 9-10
		
		assertEquals(6, multirange.size());
		assertEquals(3, multirange.getRanges().size());
		assertFalse(multirange.contains(-1));
		assertFalse(multirange.contains(0));
		assertFalse(multirange.contains(1));
		assertTrue(multirange.contains(2));
		assertTrue(multirange.contains(3));
		assertFalse(multirange.contains(4));
		assertTrue(multirange.contains(5));
		assertTrue(multirange.contains(6));
		assertFalse(multirange.contains(7));
		assertFalse(multirange.contains(8));
		assertTrue(multirange.contains(9));
		assertTrue(multirange.contains(10));
		assertFalse(multirange.contains(11));
	}
	
}
