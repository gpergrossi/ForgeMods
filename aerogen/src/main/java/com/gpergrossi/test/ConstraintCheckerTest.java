package com.gpergrossi.test;

import org.junit.Test;

import com.gpergrossi.util.data.constraints.ConstraintSolver;
import com.gpergrossi.util.data.constraints.generic.AbstractConstraint;
import com.gpergrossi.util.data.constraints.integer.SimpleScalarConstraints;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConstraintCheckerTest {
	
	SimpleScalarConstraints<Integer> ssci = SimpleScalarConstraints.forType(Integer.class);
	
	@Test
	public void testTrianglePossible() {
		ConstraintSolver<Integer> constraints = new ConstraintSolver<>(3, ssci);

		System.out.println("\nv0 <= v1:");
		assertTrue(constraints.addConstraint(0, ssci.LESS_OR_EQUAL, 1));
		constraints.print();

		System.out.println("\nv1 <= v2:");
		assertTrue(constraints.addConstraint(1, ssci.LESS_OR_EQUAL, 2));
		constraints.print();
		
		System.out.println("\nv0 >= v2:");
		assertTrue(constraints.addConstraint(0, ssci.GREATER_OR_EQUAL, 2));
		constraints.print();
		
		AbstractConstraint<Integer> c = constraints.getConstraint(0, 1);
		assertTrue(c.equals(ssci.EQUAL));
	}
	
	@Test
	public void testLongLoop() {
		int count = 20;

		ConstraintSolver<Integer> constraints = new ConstraintSolver<>(count, ssci);

		for (int i = 0; i < count-1; i++) {
			assertTrue(constraints.addConstraint(i, ssci.LESS_OR_EQUAL, i+1));
		}
		assertTrue(constraints.addConstraint(count-1, ssci.LESS_OR_EQUAL, 0));
		
		constraints.print();
		
		int testIndex = count/2;
		AbstractConstraint<Integer> c = constraints.getConstraint(testIndex, testIndex+1);
		assertTrue(c.equals(ssci.EQUAL));
	}
	
	@Test
	public void testTriangleImpossible() {
		ConstraintSolver<Integer> constraints = new ConstraintSolver<>(3, ssci);

		System.out.println("\nv0 < v1:");
		assertTrue(constraints.addConstraint(0, ssci.LESS, 1));
		constraints.print();

		System.out.println("\nv1 < v2:");
		assertTrue(constraints.addConstraint(1, ssci.LESS, 2));
		constraints.print();
		
		System.out.println("\nv2 < v0:");
		assertFalse(constraints.addConstraint(2, ssci.LESS, 1));
		System.out.println("Impossible (no change)");
		constraints.print();
		
		System.out.println("\nv2 == v0:");
		assertFalse(constraints.addConstraint(2, ssci.EQUAL, 1));
		System.out.println("Impossible (no change)");
		constraints.print();
	}
	
}
