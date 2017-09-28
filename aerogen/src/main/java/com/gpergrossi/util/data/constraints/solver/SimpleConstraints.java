package com.gpergrossi.util.data.constraints.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import scala.actors.threadpool.Arrays;

public class SimpleConstraints {
	
	public static final Constraint FAIL = Constraint.FAIL;
	public static final Constraint LESS = Constraint.LESS;
	public static final Constraint EQUAL = Constraint.EQUAL;
	public static final Constraint GREATER = Constraint.GREATER;
	public static final Constraint LESS_OR_EQUAL = Constraint.LESS_OR_EQUAL;
	public static final Constraint GREATER_OR_EQUAL = Constraint.GREATER_OR_EQUAL;
	public static final Constraint NOT_EQUAL = Constraint.NOT_EQUAL;
	public static final Constraint PASS = Constraint.PASS;
	
	public static enum Constraint {
		FAIL(0, "!!"), 
		LESS(1, "<"), EQUAL(2, "=="), LESS_OR_EQUAL(3, "<="), 
		GREATER(4, ">"), NOT_EQUAL(5, "!="), GREATER_OR_EQUAL(6, ">="),
		PASS(7, "--");
		
		/** 
		 * The bit value indicates the condition by storing allowed 
		 * comparisons as 1 bits and disallowed comparisons as 0 bits.
		 * In order from most significant to least significant the bits
		 * represent:
		 *  
		 * <p>Greater Allowed | Equal Allowed | Less Allowed</p>
		 * 
		 * Any combination of these bits is valid. All 1's means the
		 * constraint always passes and all 0's means it always fails.
		 */
		public final byte bits;
		public final String symbol; 
		
		private Constraint(int bitValue, String symbol) {
			this.bits = (byte) bitValue;
			this.symbol = symbol;
		}
		
		public Constraint opposite() {
			return Constraint.values()[SimpleConstraints.opposite(bits)];
		}
		
		@Override
		public String toString() {
			return symbol;
		}
	}
	
	private static byte[] opposites;
	private static byte[][] implications;
	
	{
		opposites = new byte[8];
		opposites[FAIL.bits] = FAIL.bits;
		opposites[LESS.bits] = GREATER.bits;
		opposites[EQUAL.bits] = EQUAL.bits;
		opposites[LESS_OR_EQUAL.bits] = GREATER_OR_EQUAL.bits;
		opposites[GREATER.bits] = LESS.bits;
		opposites[NOT_EQUAL.bits] = NOT_EQUAL.bits;
		opposites[GREATER_OR_EQUAL.bits] = LESS_OR_EQUAL.bits;
		opposites[PASS.bits] = PASS.bits;
		
		implications = new byte[8][8];
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				implications[i][j] = PASS.bits;
			}
		}
		
		// Implies A < C
		implications[LESS.bits][LESS.bits] = LESS.bits;
		implications[LESS.bits][EQUAL.bits] = LESS.bits;
		implications[EQUAL.bits][LESS.bits] = LESS.bits;
		implications[LESS.bits][LESS_OR_EQUAL.bits] = LESS.bits;
		implications[LESS_OR_EQUAL.bits][LESS.bits] = LESS.bits;
		
		// Implies A <= C
		implications[EQUAL.bits][LESS_OR_EQUAL.bits] = LESS_OR_EQUAL.bits;
		implications[LESS_OR_EQUAL.bits][EQUAL.bits] = LESS_OR_EQUAL.bits;
		implications[LESS_OR_EQUAL.bits][LESS_OR_EQUAL.bits] = LESS_OR_EQUAL.bits;
		
		// Implies A == C
		implications[EQUAL.bits][EQUAL.bits] = EQUAL.bits;
		
		// Implies A >= C
		implications[EQUAL.bits][GREATER_OR_EQUAL.bits] = GREATER_OR_EQUAL.bits;
		implications[GREATER_OR_EQUAL.bits][EQUAL.bits] = GREATER_OR_EQUAL.bits;
		implications[GREATER_OR_EQUAL.bits][GREATER_OR_EQUAL.bits] = GREATER_OR_EQUAL.bits;
		
		// Implies A > C
		implications[GREATER.bits][GREATER.bits] = GREATER.bits;
		implications[GREATER.bits][EQUAL.bits] = GREATER.bits;
		implications[EQUAL.bits][GREATER.bits] = GREATER.bits;
		implications[GREATER.bits][GREATER_OR_EQUAL.bits] = GREATER.bits;
		implications[GREATER_OR_EQUAL.bits][GREATER.bits] = GREATER.bits;
		
		// Implies A != C
		implications[EQUAL.bits][NOT_EQUAL.bits] = NOT_EQUAL.bits;
		implications[NOT_EQUAL.bits][EQUAL.bits] = NOT_EQUAL.bits;
	}
	
	/**
	 * Returns the constraintBA given constraintAB
	 * @param constraintAB - the relationship from indexA to indexB
	 * @return the relationship's opposite. I.E. constraintBA, from indexB to indexA
	 */
	private static byte opposite(byte constraintAB) {
		byte oppositeRevision = (byte) opposites[(constraintAB & MASK_REVISION_BITS)];
		byte oppositeCommitted = (byte) (opposites[(constraintAB & MASK_COMMITED_BITS) >> 4] << 4);
		
		return (byte) (oppositeCommitted | (constraintAB & UPDATE_FLAG) | oppositeRevision);
	}
	
	/**
	 * Returns the implied constraintAC given constraintAB and constraintBC.
	 * @param constraintAB - the relationship from indexA to indexB
	 * @param constraintBC - the relationship from indexB to indexC
	 * @return the implied relationship between indexA and indexC
	 */
	private static byte implied(byte constraintAB, byte constraintBC) {
		return implications[constraintAB][constraintBC];
	}
	
	private static final byte MASK_REVISION_BITS = 0x07;
	private static final byte MASK_COMMITED_BITS = 0x70;
	private static final byte UPDATE_FLAG = 0x08;
	
	private static final byte CONST_EQUAL_COMMITTED = (byte) (EQUAL.bits | (EQUAL.bits << 4));
	private static final byte CONST_PASS_COMMITTED = (byte) (PASS.bits | (PASS.bits << 4));
	
	public int numItems;
	
	/**
	 * The matrix contains each relationship constraint between (indexA, indexB)
	 * Only the relationships between an index and indices lower than it are stored.
	 * 
	 * The bits stored in this matrix are as follows:<pre>
	 * 0 | (3 bit "committed" constraint bits) | (1 bit "needs update" flag bit) | (3 bit "revision" constraint bits)
	 * </pre>
	 */
	public byte[][] constraintMatrix;
	
	public SimpleConstraints(int numItems) {
		this.numItems = numItems;

		this.constraintMatrix = new byte[numItems-1][];
		for (int i = 1; i <= numItems-1; i++) {
			this.constraintMatrix[i-1] = new byte[i];
			for (int j = 0; j < i; j++) {
				this.constraintMatrix[i-1][j] = CONST_PASS_COMMITTED;
			}
		}
	}
	
	private byte internalGetConstraint(int indexA, int indexB) {
		if (indexA < indexB) return opposite(internalGetConstraint(indexB, indexA));
		if (indexA == indexB) return CONST_EQUAL_COMMITTED;
		return constraintMatrix[indexA-1][indexB];
	}
	
	private void internalSetConstraint(int indexA, int indexB, byte constraintAB) {
		if (indexA < indexB) {
			internalSetConstraint(indexB, indexA, opposite(constraintAB));
			return;
		}
		if (indexA == indexB) return;
		constraintMatrix[indexA-1][indexB] = constraintAB;
	}
	
	/**
	 * Places the desired constraint into the revision bits for the given relationship in 
	 * the adjacency matrix. Returns false if the new constrain produces a contradiction,
	 * I.E. the value of the new revision bits is 0 (FAIL). Otherwise, returns true.
	 * Regardless of the return value, the revision bits will be modified.
	 */
	private boolean internalAddSingleConstraint(int indexA, int indexB, byte constraintAB, boolean addUpdateFlag) {
		byte original = internalGetConstraint(indexA, indexB);
		byte modified = (byte) (original & (MASK_COMMITED_BITS | UPDATE_FLAG | constraintAB));
		
		if (modified != original && addUpdateFlag) modified |= UPDATE_FLAG;
		internalSetConstraint(indexA, indexB, modified);
		
		return ((modified & MASK_REVISION_BITS) != 0);
	}
	
	private boolean internalPropagateConstraint(int indexB, int indexC, boolean doReverse) {
		boolean success = true;
		
		byte constraintBC = (byte) (internalGetConstraint(indexB, indexC) & MASK_REVISION_BITS);
		for (int indexA = 0; indexA < numItems; indexA++) {
			if (indexA == indexB || indexB == indexC || indexA == indexC) continue;
			byte constraintAB = (byte) (internalGetConstraint(indexA, indexB) & MASK_REVISION_BITS);
						
			byte implicationAC = implied(constraintAB, constraintBC);
			if ((implicationAC & MASK_REVISION_BITS) == PASS.bits) continue;

//			System.out.println("v"+indexA+" "+Constraint.values()[constraintAB]+" v"+indexB+" AND "+
//					"v"+indexB+" "+Constraint.values()[constraintBC]+" v"+indexC+" IMPLIES "+
//					"v"+indexA+" "+Constraint.values()[implicationAC]+" v"+indexC);
			success &= internalAddSingleConstraint(indexA, indexC, implicationAC, true);
		}
		
		if (doReverse) success &= internalPropagateConstraint(indexC, indexB, false);
		
		return success;
	}
	
	private boolean internalAddConstraint(int indexA, int indexB, byte constraintAB) {
		boolean success = true;
		
//		System.out.println("Adding constraint: v"+indexA+" "+Constraint.values()[constraintAB]+" v"+indexB);
		
		// Add A->B and B->A
		success &= internalAddSingleConstraint(indexA, indexB, constraintAB, true);
		if (!success) return false;
		
		boolean changes;
		do {
			changes = false;
			for (int i = 1; i <= numItems-1; i++) {
				for (int j = 0; j < i; j++) {
					if ((constraintMatrix[i-1][j] & UPDATE_FLAG) == 0) continue;
					
					constraintMatrix[i-1][j] &= ~UPDATE_FLAG;
					changes = true;
					
					success &= internalPropagateConstraint(i, j, true);
					if (!success) return false;
				}
			}
			
		} while (changes);
		
		return success;
	}
	
	private void internalCommit() {
		for (int i = 1; i <= numItems-1; i++) {
			for (int j = 0; j < i; j++) {
				byte committed = constraintMatrix[i-1][j];
				committed &= MASK_REVISION_BITS;
				committed |= (committed << 4);
				constraintMatrix[i-1][j] = committed;
			}
		}
	}
	
	private void internalRevert() {
		for (int i = 1; i <= numItems-1; i++) {
			for (int j = 0; j < i; j++) {
				byte reverted = constraintMatrix[i-1][j];
				reverted &= MASK_COMMITED_BITS;
				reverted |= (reverted >> 4);
				constraintMatrix[i-1][j] = reverted;
			}
		}
	}
	
	public boolean addConstraint(int indexA, Constraint constraint, int indexB) {
		if (indexA < 0 || indexA >= numItems) throw new IndexOutOfBoundsException("IndexA = "+indexA+" (numItems = "+numItems+")");
		if (indexB < 0 || indexB >= numItems) throw new IndexOutOfBoundsException("IndexB = "+indexB+" (numItems = "+numItems+")");
		
		boolean success = internalAddConstraint(indexA, indexB, constraint.bits);
		if (success) internalCommit();
		else internalRevert();
		
		return success;
	}

	public Constraint getConstraint(int indexA, int indexB) {
		byte bits = internalGetConstraint(indexA, indexB);
		return Constraint.values()[(bits & MASK_COMMITED_BITS) >> 4];
	}

	/**
	 * Assigns each item to a specific random value (subject to constraints)
	 * Returns null if the range provided mint to max inclusive) is not large enough.
	 * @param min
	 * @param max
	 * @param random
	 * @return
	 */
	public int[] getRandomAssignment(int min, int max, int minSpacing, Random random) {
		List<List<PriorityEntry>> trees = separateTrees();
		
		System.out.println("Entries form "+trees.size()+" distinct tree(s)");
		
		int treeNum = 0;
		for (List<PriorityEntry> tree : trees) {
			System.out.println("Tree "+treeNum+": ");

			Collections.sort(tree);
			
			// 
			List<PriorityEntry> longestPath = new ArrayList<>();
			PriorityEntry previous = null;
			for (PriorityEntry entry : sortedTree) {
				if (previous != null && entry.compareTo(previous) == 0) continue;
				longestPath.add(entry);
				previous = entry;
			}
			return longestPath;
			
			List<PriorityEntry> longestPath = getLongestPath(tree);
			
			// Assign an evenly distributed list of random numbers to the longest path
			int[] assign = new int[longestPath.size()];
			
			int range = max-min;
			int padding = minSpacing * longestPath.size();
			
			for (int i = 0; i < assign.length; i++) {
				assign[i] = random.nextInt(max-min+1)+min;
			}
			Arrays.sort(assign);
			for (int i = 0; i < assign.length; i++) {
				longestPath.get(i).value = assign[i];
			}
			
			treeNum++;
		}
		
		
		return null;
	}

	private List<List<PriorityEntry>> separateTrees() {
		List<List<PriorityEntry>> trees = new ArrayList<>();
		
		seperateTreesItem:
		for (int i = 0; i < numItems; i++) {
			PriorityEntry entry = new PriorityEntry(i);
			
			// Check existing trees for a connection
			for (List<PriorityEntry> tree : trees) {
				if (internalTreeConnectsTo(tree, entry)) {
					tree.add(entry);
					continue seperateTreesItem;
				}
			}
			
			// No trees connected, make new tree
			List<PriorityEntry> tree = new ArrayList<>();
			tree.add(entry);
			trees.add(tree);
		}
		
		return trees;
	}

	private boolean internalTreeConnectsTo(List<PriorityEntry> tree, PriorityEntry entry) {
		for (PriorityEntry treeEntry : tree) {
			Constraint c = treeEntry.getConstraintTo(entry);
			if (c == NOT_EQUAL || c == PASS) continue;
			return true;
		}
		return false;
	}

	private class PriorityEntry implements Comparable<PriorityEntry> {
		
		protected int index;
		protected int layer;
		protected List<PriorityEntry> lowerNeighbors;
		protected List<PriorityEntry> higherNeighbors;
		
		protected int value;
		
		public PriorityEntry(int index) {
			this.index = index;
			this.lowerNeighbors = new ArrayList<>();
			this.higherNeighbors = new ArrayList<>();
		}

		public Constraint getConstraintTo(PriorityEntry entry) {
			return getConstraint(this.index, entry.index);
		}

		@Override
		public int compareTo(PriorityEntry o) {
			Constraint c = this.getConstraintTo(o);
			switch (c) {
				case LESS: return -1;
				case LESS_OR_EQUAL: return -1;
				case GREATER: return 1;
				case GREATER_OR_EQUAL: return 1;
				case EQUAL: return 0;
				
				case FAIL:
				case NOT_EQUAL:
				case PASS:
				default:
					return this.hashCode() - o.hashCode();
			}
		}		
	}
	
	public void print() {
		int charsPerNum = 2;
		if (numItems > 10) charsPerNum++;
		if (numItems > 100) charsPerNum++;
		
		String pad = "";
		for (int i = 0; i < charsPerNum; i++) {
			pad += " ";
		}

		System.out.print(pad);
		for (int j = 0; j < numItems; j++) {
			String label = pad+"v"+j;
			label = label.substring(label.length() - charsPerNum);
			System.out.print(" "+label);
		}
		System.out.println();
		
		for (int i = 0; i < numItems; i++) {
			String label = pad+"v"+i;
			label = label.substring(label.length() - charsPerNum);
			System.out.print(label);
			
			for (int j = 0; j < numItems; j++) {
				String value = pad+getConstraint(i, j).toString();
				value = value.substring(value.length() - charsPerNum);
				System.out.print(" "+value);
			}
			
			System.out.println();
		}
	}
	
}
