package com.gpergrossi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.gpergrossi.util.data.constraints.IntegerConstraint;
import com.gpergrossi.util.data.constraints.AbstractConstraint.Category;
import com.gpergrossi.util.data.constraints.ConstraintSystem;

import static com.gpergrossi.util.data.constraints.IntegerConstraint.*;

public class IntegerConstraintTest {
	
	@Test
	public void testEqual() {
		IntegerConstraint ab = EQUAL;
		
		assertTrue(ab.isPossible());
		assertFalse(ab.isGuaranteed());
		
		assertTrue(ab.equals(EQUAL));
		assertTrue(ab.equals(equal(0)));
		assertFalse(ab.equals(LESS));
		assertFalse(ab.equals(equal(1)));

		assertTrue(ab.check(Integer.MIN_VALUE, Integer.MIN_VALUE));
		assertTrue(ab.check(-10000, -10000));
		assertTrue(ab.check(-1, -1));
		assertTrue(ab.check(0, 0));
		assertTrue(ab.check(1, 1));
		assertTrue(ab.check(10000, 10000));
		assertTrue(ab.check(Integer.MAX_VALUE, Integer.MAX_VALUE));
		
		assertFalse(ab.check(0, 1));
		assertFalse(ab.check(1, -1));
		assertFalse(ab.check(Integer.MAX_VALUE, Integer.MIN_VALUE));
		
		assertEquals(" == ", ab.inline());
	}
	
	@Test
	public void testNotEqual() {
		IntegerConstraint ab = NOT_EQUAL;
		
		assertTrue(ab.isPossible());
		assertFalse(ab.isGuaranteed());
		
		assertTrue(ab.equals(NOT_EQUAL));
		assertTrue(ab.equals(notEqual(+0)));
		assertFalse(ab.equals(EQUAL));
		assertFalse(ab.equals(notEqual(+1)));

		assertTrue(ab.check(Integer.MIN_VALUE, Integer.MAX_VALUE));
		assertTrue(ab.check(-10000, -10001));
		assertTrue(ab.check(-1, 0));
		assertTrue(ab.check(0, 1));
		assertTrue(ab.check(10001, 10000));
		assertTrue(ab.check(Integer.MAX_VALUE, Integer.MIN_VALUE));
		
		assertFalse(ab.check(0, 0));
		assertFalse(ab.check(1, 1));
		assertFalse(ab.check(-1, -1));
		assertFalse(ab.check(Integer.MAX_VALUE, Integer.MAX_VALUE));
		assertFalse(ab.check(Integer.MIN_VALUE, Integer.MIN_VALUE));
		
		assertEquals(" != ", ab.inline());
	}
	
	@Test
	public void testLess() {
		IntegerConstraint ab = LESS;
		
		assertTrue(ab.isPossible());
		assertFalse(ab.isGuaranteed());
		
		assertTrue(ab.equals(LESS));
		assertTrue(ab.equals(less(+0)));
		assertFalse(ab.equals(EQUAL));
		assertFalse(ab.equals(less(+1)));

		assertTrue(ab.check(Integer.MIN_VALUE, Integer.MAX_VALUE));
		assertTrue(ab.check(-10001, -10000));
		assertTrue(ab.check(-1, 0));
		assertTrue(ab.check(0, 1));
		assertTrue(ab.check(10000, 10001));
		
		assertFalse(ab.check(0, 0));
		assertFalse(ab.check(1, 1));
		assertFalse(ab.check(-1, -1));
		assertFalse(ab.check(Integer.MIN_VALUE, Integer.MIN_VALUE));
		assertFalse(ab.check(Integer.MAX_VALUE, Integer.MIN_VALUE));
		assertFalse(ab.check(1, 0));
		
		assertEquals(" < ", ab.inline());
	}
	
	@Test
	public void testLessOrEqual() {
		IntegerConstraint ab = LESS_OR_EQUAL;
		
		assertTrue(ab.isPossible());
		assertFalse(ab.isGuaranteed());
		
		assertTrue(ab.equals(LESS_OR_EQUAL));
		assertTrue(ab.equals(lessOrEqual(+0)));
		assertTrue(ab.equals(less(+1)));
		assertFalse(ab.equals(LESS));
		assertFalse(ab.equals(EQUAL));
		assertFalse(ab.equals(lessOrEqual(+1)));

		assertTrue(ab.check(Integer.MIN_VALUE, Integer.MAX_VALUE));
		assertTrue(ab.check(-10001, -10000));
		assertTrue(ab.check(-1, 0));
		assertTrue(ab.check(0, 1));
		assertTrue(ab.check(10000, 10001));
		assertTrue(ab.check(0, 0));
		assertTrue(ab.check(1, 1));
		assertTrue(ab.check(-1, -1));
		assertTrue(ab.check(Integer.MIN_VALUE, Integer.MIN_VALUE));
		
		assertFalse(ab.check(Integer.MAX_VALUE, Integer.MIN_VALUE));
		assertFalse(ab.check(1, 0));
		
		assertEquals(" <= ", ab.inline());
	}
	
	@Test
	public void testGreater() {
		IntegerConstraint ab = GREATER;
		
		assertTrue(ab.isPossible());
		assertFalse(ab.isGuaranteed());
		
		assertTrue(ab.equals(GREATER));
		assertTrue(ab.equals(greater(+0)));
		assertFalse(ab.equals(EQUAL));
		assertFalse(ab.equals(greater(+1)));

		assertTrue(ab.check(Integer.MAX_VALUE, Integer.MIN_VALUE));
		assertTrue(ab.check(-10000, -10001));
		assertTrue(ab.check(0, -1));
		assertTrue(ab.check(1, 0));
		assertTrue(ab.check(10001, 10000));
		
		assertFalse(ab.check(0, 0));
		assertFalse(ab.check(1, 1));
		assertFalse(ab.check(-1, -1));
		assertFalse(ab.check(Integer.MIN_VALUE, Integer.MIN_VALUE));
		assertFalse(ab.check(Integer.MIN_VALUE, Integer.MAX_VALUE));
		assertFalse(ab.check(0, 1));
		
		assertEquals(" > ", ab.inline());
	}
	
	@Test
	public void testGreaterOrEqual() {
		IntegerConstraint ab = GREATER_OR_EQUAL;
		
		assertTrue(ab.isPossible());
		assertFalse(ab.isGuaranteed());
		
		assertTrue(ab.equals(GREATER_OR_EQUAL));
		assertTrue(ab.equals(greaterOrEqual(+0)));
		assertTrue(ab.equals(greater(-1)));
		assertFalse(ab.equals(EQUAL));
		assertFalse(ab.equals(greaterOrEqual(-1)));
		
		assertTrue(ab.check(Integer.MAX_VALUE, Integer.MIN_VALUE));
		assertTrue(ab.check(-10000, -10001));
		assertTrue(ab.check(0, -1));
		assertTrue(ab.check(1, 0));
		assertTrue(ab.check(10001, 10000));
		assertTrue(ab.check(0, 0));
		assertTrue(ab.check(1, 1));
		assertTrue(ab.check(-1, -1));
		assertTrue(ab.check(Integer.MIN_VALUE, Integer.MIN_VALUE));
		
		assertFalse(ab.check(Integer.MIN_VALUE, Integer.MAX_VALUE));
		assertFalse(ab.check(0, 1));
		
		assertEquals(" >= ", ab.inline());
	}
	
	@Test
	public void testNever() {
		IntegerConstraint ab = NEVER;
		
		assertFalse(ab.isPossible());
		assertFalse(ab.isGuaranteed());
		
		assertTrue(ab.equals(NEVER));
		assertFalse(ab.equals(EQUAL));
		
		assertFalse(ab.check(Integer.MAX_VALUE, Integer.MIN_VALUE));
		assertFalse(ab.check(-10000, -10001));
		assertFalse(ab.check(0, -1));
		assertFalse(ab.check(1, 0));
		assertFalse(ab.check(10001, 10000));
		assertFalse(ab.check(0, 0));
		assertFalse(ab.check(1, 1));
		assertFalse(ab.check(-1, -1));
		assertFalse(ab.check(Integer.MIN_VALUE, Integer.MIN_VALUE));
		assertFalse(ab.check(Integer.MIN_VALUE, Integer.MAX_VALUE));
		assertFalse(ab.check(0, 1));
		
		assertEquals(" allows no ", ab.inline());
	}
	
	@Test
	public void testAlways() {
		IntegerConstraint ab = ALWAYS;
		
		assertTrue(ab.isPossible());
		assertTrue(ab.isGuaranteed());
		
		assertTrue(ab.equals(ALWAYS));
		assertFalse(ab.equals(EQUAL));
		
		assertTrue(ab.check(Integer.MAX_VALUE, Integer.MIN_VALUE));
		assertTrue(ab.check(-10000, -10001));
		assertTrue(ab.check(0, -1));
		assertTrue(ab.check(1, 0));
		assertTrue(ab.check(10001, 10000));
		assertTrue(ab.check(0, 0));
		assertTrue(ab.check(1, 1));
		assertTrue(ab.check(-1, -1));
		assertTrue(ab.check(Integer.MIN_VALUE, Integer.MIN_VALUE));
		assertTrue(ab.check(Integer.MIN_VALUE, Integer.MAX_VALUE));
		assertTrue(ab.check(0, 1));
		
		assertEquals(" allows any ", ab.inline());
	}
	
	@Test
	public void testAnd() {
		IntegerConstraint a = LESS_OR_EQUAL;
		IntegerConstraint b = GREATER_OR_EQUAL;
		IntegerConstraint c = a.and(b);
		assertTrue(c.equals(EQUAL));
		
		a = LESS;
		b = less(-7);
		c = a.and(b);
		assertTrue(c.equals(less(-7)));
		
		a = GREATER;
		b = LESS;
		c = a.and(b);
		assertTrue(c.equals(NEVER));
	}
	
	@Test
	public void testOr() {
		IntegerConstraint a = LESS;
		IntegerConstraint b = GREATER;
		IntegerConstraint c = a.or(b);
		assertTrue(c.equals(NOT_EQUAL));
		
		a = GREATER;
		b = greaterOrEqual(-7);
		c = a.or(b);
		assertTrue(c.equals(greaterOrEqual(-7)));
		
		a = GREATER;
		b = LESS_OR_EQUAL;
		c = a.or(b);
		assertTrue(c.equals(ALWAYS));
		
		a = less(+5);
		b = greater(+5);
		c = a.or(b);
		assertTrue(c.equals(notEqual(+5)));
	}
	
	@Test
	public void testImplication() {
		IntegerConstraint ab = LESS;
		IntegerConstraint bc = LESS;
		IntegerConstraint ac = ab.chain(bc);
		assertTrue(ac.equals(less(-1)));
		
		ab = lessOrEqual(+10);
		bc = lessOrEqual(+20);
		ac = ab.chain(bc);
		assertTrue(ac.equals(lessOrEqual(+30)));
	}
	
	@Test
	public void testCompliment() {
		assertTrue(NEVER.compliment().equals(ALWAYS));
		assertTrue(LESS.compliment().equals(GREATER_OR_EQUAL));
		assertTrue(LESS_OR_EQUAL.compliment().equals(GREATER));
		assertTrue(EQUAL.compliment().equals(NOT_EQUAL));
		assertTrue(NOT_EQUAL.compliment().equals(EQUAL));
		assertTrue(GREATER_OR_EQUAL.compliment().equals(LESS));
		assertTrue(GREATER.compliment().equals(LESS_OR_EQUAL));
		assertTrue(ALWAYS.compliment().equals(NEVER));
	}
	
	@Test
	public void testReverse() {
		assertTrue(NEVER.reverse().equals(NEVER));
		assertTrue(LESS.reverse().equals(GREATER));
		assertTrue(LESS_OR_EQUAL.reverse().equals(GREATER_OR_EQUAL));
		assertTrue(EQUAL.reverse().equals(EQUAL));
		assertTrue(NOT_EQUAL.reverse().equals(NOT_EQUAL));
		assertTrue(GREATER_OR_EQUAL.reverse().equals(LESS_OR_EQUAL));
		assertTrue(GREATER.reverse().equals(LESS));
		assertTrue(ALWAYS.reverse().equals(ALWAYS));
	}
	
	@Test
	public void testInline() {
		IntegerConstraint c;
		
		c = less(+31);
		assertEquals(" < 31 + ", c.inline());
		c = less(-1);
		assertEquals(" < -1 + ", c.inline());
		
		c = notEqual(-17);
		assertEquals(" != -17 + ", c.inline());
		c = notEqual(+21);
		assertEquals(" != 21 + ", c.inline());
		
		c = greater(-42);
		assertEquals(" > -42 + ", c.inline());
		c = greater(+6);
		assertEquals(" > 6 + ", c.inline());
		
		c = equal(-4);
		assertEquals(" == -4 + ", c.inline());
		c = equal(+12);
		assertEquals(" == 12 + ", c.inline());
		
		c = greaterOrEqual(-16).and(lessOrEqual(+17));
		assertEquals(" == {-16 to 17} + ", c.inline());
	}
	
	@Test
	public void testMultipleRanges() {
		IntegerConstraint a = greater(+5).and(less(+10));
		IntegerConstraint b = greater(-15).and(less(-10));
		IntegerConstraint c = a.or(b);
		assertEquals(" == {-14 to -11, 6 to 9} + ", c.inline());
	}
	
	@Test
	public void testCategory() {
		Category<IntegerConstraint> category = ALWAYS.getCategory();
		assertTrue(category.getNeverConstraint().equals(NEVER));
		assertTrue(category.getEqualConstraint().equals(EQUAL));
		assertTrue(category.getAlwaysConstraint().equals(ALWAYS));
	}
	
//	@Test
//	public void testConstraintSystem() {
//		ConstraintSystem<IntegerConstraint, Integer> system = new ConstraintSystem<>(CATEGORY, 7);
//		system.addConstraint(0, LESS, 1);
//		system.addConstraint(1, LESS, 2);
//		system.addConstraint(2, LESS, 3);
//		system.addConstraint(1, EQUAL, 4);
//		system.addConstraint(4, LESS, 5);
//		system.addConstraint(5, GREATER, 0);
//		
//		system.addConstraint(6, GREATER, 0);
//		system.addConstraint(6, LESS, 2);
//		system.addConstraint(6, NOT_EQUAL, 1);
//		
//		system.print();
//	}
	
//	@Test
//	public void testConstraintSystem() {
//		ConstraintSystem<IntegerConstraint, Integer> system = new ConstraintSystem<>(IntegerConstraint.CATEGORY, 4);
//		system.addConstraint(0, LESS_OR_EQUAL, 1);
//		system.addConstraint(1, LESS_OR_EQUAL, 3);
//		
//		system.addConstraint(0, LESS_OR_EQUAL, 2);
//		system.addConstraint(2, LESS_OR_EQUAL, 3);
//		
//		system.addConstraint(2, less(-2).or(greater(+2)), 1);
//		
//		// Now the v3->v0 relationship should be: v3 >= 2 + v0. Equivalently: v3 > 3 + v0. 
//		// This is because v1 and v2 must be 2 units apart and both sit between v0 and v3.
//		// In order for the solver to understand this
//		assertTrue(system.getConstraint(3, 0).equals(greater(+3)));
//		system.print();		
//	}
	
//	@Test
//	public void testConstraintSystem2() {
//		ConstraintSystem<IntegerConstraint, Integer> system = new ConstraintSystem<>(IntegerConstraint.CATEGORY, 6);
//		
//		int A = 0, B = 1, C = 2, D = 3, E = 4, F = 5;
//		
//		// B through E are between A and F
//		for (int i = 1; i <= 4; i++) {
//			assertTrue(system.addConstraint(A, LESS, i));
//			assertTrue(system.addConstraint(i, LESS, F));
//		}
//		assertTrue(system.addConstraint(F, equal(+10), A));
//
//		assertTrue(system.addConstraint(E, equal(+5).or(equal(-5)), B));
//		assertTrue(system.addConstraint(E, less(-1), F));
//		
//		assertTrue(system.addConstraint(D, greater(+2).or(less(-2)), B));
//		assertTrue(system.addConstraint(D, less(-1).or(greater(+1)), E));
//		
//		assertTrue(system.addConstraint(C, equal(+1).or(equal(-1)), D));
//		assertTrue(system.addConstraint(C, less(-1).or(greater(+1)), E));
//
//		assertTrue(system.addConstraint(B, greater(+2), A));
//		assertTrue(system.addConstraint(B, equal(+6), A));
//		
//		system.print();
//		
//		system.addConstraint(C, equal(+4), A);
//		
//		system.print();
//
//		
//		// Solution:
//		// 01234567890
//		// A  B CD E F
//	}
	
	@Test
	public void testConstraintSystemSudoku2x2() {
		ConstraintSystem<IntegerConstraint, Integer> system = new ConstraintSystem<>(IntegerConstraint.CATEGORY, 17);
		
		int[][] cells = new int[4][4];
		
		// All values between 1 and 4
		for (int j = 0; j < 4; j++) {
			for (int i = 0; i < 4; i++) {
				cells[i][j] = j*4+i+1;
				assertTrue(system.addConstraint(cells[i][j], greaterOrEqual(1).and(lessOrEqual(4)), 0));
			}
		}
		
		// No row duplicates
		for (int j = 0; j < 4; j++) {
			for (int i1 = 0; i1 < 4; i1++) {
				for (int i2 = i1+1; i2 < 4; i2++) {
					assertTrue(system.addConstraint(cells[i1][j], NOT_EQUAL, cells[i2][j]));
				}
			}
		}

		// No column duplicates
		for (int i = 0; i < 4; i++) {
			for (int j1 = 0; j1 < 4; j1++) {
				for (int j2 = j1+1; j2 < 4; j2++) {
					assertTrue(system.addConstraint(cells[i][j1], NOT_EQUAL, cells[i][j2]));
				}
			}
		}

		// No cell duplicates
		assertTrue(system.addConstraint(cells[0][0], NOT_EQUAL, cells[1][1]));
		assertTrue(system.addConstraint(cells[0][1], NOT_EQUAL, cells[1][0]));
		assertTrue(system.addConstraint(cells[2][0], NOT_EQUAL, cells[3][1]));
		assertTrue(system.addConstraint(cells[2][1], NOT_EQUAL, cells[3][0]));
		assertTrue(system.addConstraint(cells[0][2], NOT_EQUAL, cells[1][3]));
		assertTrue(system.addConstraint(cells[0][3], NOT_EQUAL, cells[1][2]));
		assertTrue(system.addConstraint(cells[2][2], NOT_EQUAL, cells[3][3]));
		assertTrue(system.addConstraint(cells[2][3], NOT_EQUAL, cells[3][2]));
		
		system.print();

		assertTrue(system.addConstraint(cells[0][0], equal(3), 0));
		assertTrue(system.addConstraint(cells[3][0], equal(2), 0));
		assertTrue(system.addConstraint(cells[2][1], equal(3), 0));
		assertTrue(system.addConstraint(cells[1][2], equal(1), 0));
		assertTrue(system.addConstraint(cells[0][3], equal(2), 0));
		assertTrue(system.addConstraint(cells[3][3], equal(1), 0));

		system.print();
		
		// Solution:
		// 01234567890
		// A  B CD E F
	}
	
}