package com.gpergrossi.util.data.constraints;

import java.io.PrintStream;

public class ConstraintSystem<ConstraintClass extends AbstractConstraint<ConstraintClass, ValueClass>, ValueClass> {

	private final int numItems;
	private final AbstractConstraint.Category<ConstraintClass> constraintsCategory;
	private final AbstractConstraint<?, ?>[][] constraints;
	private final boolean[][] hasChanged;

	private final AbstractConstraint<?, ?>[][] saveState;
	private ConstraintClass lastConstraint;
	private int lastConstraintA, lastConstraintB;
	
	public ConstraintSystem(AbstractConstraint.Category<ConstraintClass> constraintsCategory, int numItems) {
		this.constraintsCategory = constraintsCategory;
		this.numItems = numItems;
		
		this.saveState = new AbstractConstraint<?, ?>[numItems-1][];
		this.constraints = new AbstractConstraint<?, ?>[numItems-1][];
		this.hasChanged = new boolean[numItems-1][];
		for (int i = 1; i <= numItems-1; i++) {
			this.saveState[i-1] = new AbstractConstraint<?, ?>[i];
			this.constraints[i-1] = new AbstractConstraint<?, ?>[i];
			this.hasChanged[i-1] = new boolean[i];
			for (int j = 0; j < i; j++) {
				this.saveState[i-1][j] = constraintsCategory.getAlwaysConstraint();
				this.constraints[i-1][j] = constraintsCategory.getAlwaysConstraint();
			}
		}
	}

	/**
	 * Gets the constraint stored in the matrix for AB. Since the constraint matrix 
	 * is a lower half matrix, this involves looking up the correct order of the given
	 * indices, and potentially reversing the constraint to be returned. These internals
	 * are, of course, hidden from users of the method.
	 * @param indexA
	 * @param indexB
	 * @return the constrain from indexA to indexB that is stored in the constraints matrix
	 */
	@SuppressWarnings("unchecked")
	private ConstraintClass internalGetConstraint(int indexA, int indexB) {
		if (indexA < indexB) return internalGetConstraint(indexB, indexA).reverse();
		if (indexA == indexB) return constraintsCategory.getEqualConstraint();
		return (ConstraintClass) constraints[indexA-1][indexB];
	}
	
	/**
	 * Sets the constraint stored in the matrix for AB. If the hasChanged parameter is true,
	 * the has 
	 * @param indexA
	 * @param indexB
	 * @return the constrain from indexA to indexB that is stored in the constraints matrix
	 */
	private void internalSetConstraint(int indexA, int indexB, ConstraintClass constraintAB, boolean hasChanged) {
		if (indexA < indexB) internalSetConstraint(indexB, indexA, constraintAB.reverse(), hasChanged);
		else if (indexA == indexB) {
			if (constraintAB.equals(constraintsCategory.getEqualConstraint())) return;
			else throw new RuntimeException("internalSetConstraint has just asserted that [v"+indexA+constraintAB.inline()+"v"+indexB+"]");
		} else {
			this.constraints[indexA-1][indexB] = constraintAB;
			this.hasChanged[indexA-1][indexB] = hasChanged;
		}
	}
	
	/**
	 * Places the desired constraint into the revision bits for the given relationship in 
	 * the adjacency matrix. Returns false if the new constraint produces a contradiction,
	 * I.E. the value of the new revision bits is 0 (FAIL). Otherwise, returns true.
	 * Regardless of the return value, the revision bits will be modified.
	 */
	private boolean internalAddSingleConstraint(int indexA, int indexB, ConstraintClass constraintAB, PrintStream debugStream) {
		ConstraintClass original = internalGetConstraint(indexA, indexB);
		ConstraintClass modified = original.and(constraintAB);
		
		boolean updated = !modified.equals(original);
		internalSetConstraint(indexA, indexB, modified, updated);

		boolean possible = modified.isPossible();
		if (debugStream != null && !possible) debugStream.println("Impossible constraint between v"+indexA+" and v"+indexB);
		return possible;
	}
	
	private boolean internalPropagateConstraint(int indexA, int indexB, boolean doReverse, PrintStream debugStream) {
		if (indexA == indexB) return true;
		
		boolean possible = true;
		ConstraintClass constraintAB = internalGetConstraint(indexA, indexB);
		for (int indexC = 0; indexC < numItems; indexC++) {
			if (indexA == indexC || indexB == indexC) continue;
			ConstraintClass constraintBC = internalGetConstraint(indexB, indexC);
			
			ConstraintClass implicationAC = constraintAB.chain(constraintBC);
			if (implicationAC.isGuaranteed()) continue;

			if (debugStream != null) {
				ConstraintClass oldConstraintAC = getConstraint(indexA, indexC);
				ConstraintClass newConstraintAC = implicationAC.and(oldConstraintAC);
				if (!newConstraintAC.equals(oldConstraintAC)) {
					debugStream.println("[v"+indexA+constraintAB.inline()+"v"+indexB+"]   AND   ["+
						"v"+indexB+constraintBC.inline()+"v"+indexC+"]   IMPLIES   ["+
						"v"+indexA+newConstraintAC.inline()+"v"+indexC+"]");
				}
			}
			
			possible &= internalAddSingleConstraint(indexA, indexC, implicationAC, debugStream);
		}
		
		if (doReverse) possible &= internalPropagateConstraint(indexB, indexA, false, debugStream);
		
		return possible;
	}
	
	private boolean internalAddConstraint(int indexA, int indexB, ConstraintClass constraintAB, PrintStream debugStream) {
		boolean possible = true;
		if (debugStream != null) debugStream.println("Adding constraint: [v"+indexA+constraintAB.inline()+"v"+indexB+"]");
		
		possible &= internalAddSingleConstraint(indexA, indexB, constraintAB, debugStream);
		if (!possible) return false;
		
		boolean changes = true;
		while (changes) {
			changes = false;
			for (int i = 1; i <= numItems-1; i++) {
				for (int j = 0; j < i; j++) {
					if (!hasChanged[i-1][j]) continue;
					
					hasChanged[i-1][j] = false;
					changes = true;
					
					possible &= internalPropagateConstraint(i, j, true, debugStream);
					if (!possible) return false;
				}
			}
		}
		
		return possible;
	}
	
	public boolean addConstraint(int indexA, ConstraintClass constraint, int indexB) {		
		if (indexA < 0 || indexA >= numItems) throw new IndexOutOfBoundsException("IndexA = "+indexA+" (numItems = "+numItems+")");
		if (indexB < 0 || indexB >= numItems) throw new IndexOutOfBoundsException("IndexB = "+indexB+" (numItems = "+numItems+")");
		
		// Save state before changes
		lastConstraint = constraint;
		lastConstraintA = indexA;
		lastConstraintB = indexB;
		createBackup(saveState);
		
		// Try to apply the constraint
		boolean success = internalAddConstraint(indexA, indexB, constraint, null);
		
		// Restore if failure
		if (!success) restoreBackup(saveState);
		
		return success;
	}

	private AbstractConstraint<?, ?>[][] createBackup(AbstractConstraint<?, ?>[][] backup) {
		if (backup == null || backup.length < numItems-1) {
			backup = new AbstractConstraint<?, ?>[numItems-1][];
		}
		for (int i = 1; i <= numItems-1; i++) {
			if (backup[i-1] == null || backup[i-1].length < i) {
				backup[i-1] = new AbstractConstraint<?, ?>[i];	
			}
			System.arraycopy(constraints[i-1], 0, backup[i-1], 0, i);
		}
		return backup;
	}
	
	private void restoreBackup(AbstractConstraint<?, ?>[][] backup) {
		for (int i = 1; i <= numItems-1; i++) {
			System.arraycopy(backup[i-1], 0, constraints[i-1], 0, i);
			for (int j = 0; j < i; j++) {
				hasChanged[i-1][j] = false;
			}
		} 
	}

	public ConstraintClass getConstraint(int indexA, int indexB) {
		return internalGetConstraint(indexA, indexB);
	}
	
	private String extend(String str, int length) {
		String out = "";
		while (out.length() < length) {
			out += str;
		}
		return out.substring(0, length);
	}
	
	public void print() {
		int[] cellWidth = new int[numItems];
		int minCellWidth = (int) Math.ceil(Math.log10(numItems)) + 2; // +2 for "v" and " " in "v## |"
		int maxCellWidth = minCellWidth;
		for (int j = 0; j < numItems; j++) {
			cellWidth[j] = (int) Math.ceil(Math.log10(numItems));
			for (int i = 0; i < numItems; i++) {
				cellWidth[j] = Math.max(cellWidth[j], getConstraint(i, j).inline().length());
			}
			maxCellWidth = Math.max(maxCellWidth, cellWidth[j]);
		}
		
		String emptryCellStr = extend(" ", maxCellWidth);
		String dashed = extend("-", maxCellWidth);
		
		// Label Row
		System.out.print(emptryCellStr.substring(0, minCellWidth)+"|");
		for (int j = 0; j < numItems; j++) {
			String label = emptryCellStr+"v"+j+" ";
			label = label.substring(label.length() - cellWidth[j]);
			System.out.print(label+"|");
		}
		System.out.println();
		
		// Label Row Divider ------+------+----- 
		System.out.print(dashed.substring(0, minCellWidth)+"+");
		for (int j = 0; j < numItems; j++) {
			String label = dashed.substring(dashed.length() - cellWidth[j]);
			System.out.print(label+"+");
		}
		System.out.println();
		
		for (int i = 0; i < numItems; i++) {
			String label = emptryCellStr+"v"+i+" ";
			label = label.substring(label.length() - minCellWidth);
			System.out.print(label+"|");
			
			for (int j = 0; j < numItems; j++) {
				String value = emptryCellStr+getConstraint(i, j).inline();
				value = value.substring(value.length() - cellWidth[j]);
				System.out.print(value+"|");
			}
			
			System.out.println();
		}
		System.out.println();
	}

	public void printExplanation() {
		printExplanation(0, 0);
	}

	public void printExplanation(int indexA, int indexB) {
		if (lastConstraint == null) {
			System.out.println("No constraint to explain");
			return;
		}

		ConstraintClass beforeAB = null, afterAB = null;
		
		// Revert to before previous constraint
		restoreBackup(saveState);
		if (indexA != indexB) {
			beforeAB = getConstraint(indexA, indexB);
			System.out.println("Before: [v"+indexA+beforeAB.inline()+"v"+indexB+"]");
			System.out.println();
		}
		
		// Re-apply constraint with debug print
		internalAddConstraint(lastConstraintA, lastConstraintB, lastConstraint, System.out);
		if (indexA != indexB) {
			afterAB = getConstraint(indexA, indexB);
			System.out.println();
			System.out.println("After: [v"+indexA+afterAB.inline()+"v"+indexB+"]");
			System.out.println();
			
			ConstraintClass nowGone = beforeAB.and(afterAB.compliment());
			// Any values in nowGone?
			if (nowGone.isPossible()) {
				System.out.println("Now [v"+indexA+nowGone.inline()+"v"+indexB+"] is impossible");
			}
		}
	}
	
}
