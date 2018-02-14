package com.gpergrossi.test;

import static com.gpergrossi.constraints.integer.IntegerConstraint.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import com.gpergrossi.constraints.generic.IConstraintClass;
import com.gpergrossi.constraints.integer.IntegerConstraint;
import com.gpergrossi.constraints.integer.IntegerSolver;
import com.gpergrossi.constraints.matrix.ConstraintMatrix;

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
	public void testReverse() {
		assertTrue(NEVER.reverse().equals(NEVER));
		assertTrue(LESS.reverse().equals(GREATER));
		assertTrue(LESS_OR_EQUAL.reverse().equals(GREATER_OR_EQUAL));
		assertTrue(EQUAL.reverse().equals(EQUAL));
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
	public void testClass() {
		IConstraintClass<IntegerConstraint> category = ALWAYS.getConstraintClass();
		assertTrue(category.getNeverConstraint().equals(NEVER));
		assertTrue(category.getEqualConstraint().equals(EQUAL));
		assertTrue(category.getAlwaysConstraint().equals(ALWAYS));
	}
	
	@Test
	public void testConstraintSystem() {
		ConstraintMatrix<IntegerConstraint> matrix = new ConstraintMatrix<>(IntegerConstraint.CLASS, 4);
		assertTrue(matrix.andConstraint(0, LESS, 1));
		assertTrue(matrix.andConstraint(2, LESS, 1));
		assertTrue(matrix.andConstraint(2, LESS, 3));
	}
	
	@Test
	public void testConstraintContradiction() {
		ConstraintMatrix<IntegerConstraint> matrix = new ConstraintMatrix<>(IntegerConstraint.CLASS, 3);
		assertTrue(matrix.andConstraint(0, LESS, 1));
		assertTrue(matrix.andConstraint(1, LESS, 2));
		
		// Test a failed constraint and make sure it doesn't change the matrix
		IntegerConstraint before = matrix.getMatrixEntry(2, 0).getConstraint();
		assertFalse(matrix.andConstraint(2, LESS, 0));
		IntegerConstraint after = matrix.getMatrixEntry(2, 0).getConstraint();
		assertEquals(before, after);
	}
	
	@Test
	public void testConstraintSolver() {
		ConstraintMatrix<IntegerConstraint> matrix = new ConstraintMatrix<>(IntegerConstraint.CLASS, 4);
		matrix.andConstraint(0, LESS, 1);
		matrix.andConstraint(1, LESS, 3);
		
		matrix.andConstraint(0, LESS, 2);
		matrix.andConstraint(2, LESS, 3);
		
		matrix.andConstraint(1, LESS, 2);
		
		matrix.andConstraint(3, greaterOrEqual(0).and(lessOrEqual(10)), 0);
		
		Random random = new Random(758423312L);
		IntegerSolver solver = new IntegerSolver(random);
		
		matrix.print();
		ConstraintMatrix<IntegerConstraint> solution = solver.solve(matrix);
		solution.print();
	}
	
	@Test
	public void testConstraintSolverIslandSim() {
		int numIslands = 40;
		int minAltitude = 16;
		int maxAltitude = 128;
		int numConstraintAttempts = 70;
		Random random = new Random(758423312L);
		
		ConstraintMatrix<IntegerConstraint> matrix = new ConstraintMatrix<>(IntegerConstraint.CLASS, numIslands+1);
		for (int i = 0; i < numIslands; i++) {
			matrix.andConstraint(i+1, greaterOrEqual(minAltitude).and(lessOrEqual(maxAltitude)), 0);
		}
		
		for (int i = 0; i < numConstraintAttempts; i++) {
			// Two different islands
			int islandA = random.nextInt(numIslands);
			int islandB = random.nextInt(numIslands-1);
			if (islandB >= islandA) islandB++;
			
			// Random constraint
			int typeRoll = random.nextInt(3);
			switch (typeRoll) {
				case 0:	{ 
					// Equals 
					final boolean success = matrix.andConstraint(islandA, EQUAL, islandB);
					if (success) System.out.println("Island "+islandA+" is EQUAL altitude to Island "+islandB);
					else System.out.println("--");
					break;
				}
				case 1:	{
					// Less
					boolean success = matrix.andConstraint(islandA, less(-16), islandB);
					if (success) System.out.println("Island "+islandA+" is lower altitude than Island "+islandB);
					else System.out.println("--");
					break;
				}
				case 2:	{
					// Greater
					boolean success = matrix.andConstraint(islandA, greater(+16), islandB);
					if (success) System.out.println("Island "+islandA+" is higher altitude than Island "+islandB);
					else System.out.println("--");
					break;
				}
				default: break;
			}
		}
		
		IntegerSolver solver = new IntegerSolver(random);
		
		ConstraintMatrix<IntegerConstraint> solution = solver.solve(matrix);
		solution.print();
	}
	
}