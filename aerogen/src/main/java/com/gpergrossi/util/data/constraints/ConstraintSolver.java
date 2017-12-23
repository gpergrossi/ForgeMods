package com.gpergrossi.util.data.constraints;

import com.gpergrossi.util.data.constraints.generic.AbstractConstraint;

public class ConstraintSolver<T> {

	protected final int numItems;
	protected final AbstractConstraint.Category<T> constraintsCategory;
	protected final AbstractConstraint<?>[][] constraints;
	protected final AbstractConstraint<?>[][] saveState;
	protected final boolean[][] hasChanged;
	
	public ConstraintSolver(AbstractConstraint.Category<T> constraintsCategory, int numItems) {
		this.constraintsCategory = constraintsCategory;
		this.numItems = numItems;
		
		this.saveState = new AbstractConstraint<?>[numItems-1][];
		this.constraints = new AbstractConstraint<?>[numItems-1][];
		this.hasChanged = new boolean[numItems-1][];
		init();
	}
	
	/**
	 * Sets the initial constraint matrix so that there are no constraints (ALWAYS constraint)
	 * between any distinct variables and so that each variable is equal to itself (EQUAL constraint).
	 */
	private void init() {
		for (int i = 1; i <= numItems-1; i++) {
			this.saveState[i-1] = new AbstractConstraint<?>[i];
			this.constraints[i-1] = new AbstractConstraint<?>[i];
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
	private AbstractConstraint<T> internalGetConstraint(int indexA, int indexB) {
		if (indexA < indexB) return internalGetConstraint(indexB, indexA).reverse();
		if (indexA == indexB) return constraintsCategory.getEqualConstraint();
		return (AbstractConstraint<T>) constraints[indexA-1][indexB];
	}
	
	/**
	 * Sets the constraint stored in the matrix for AB. If the hasChanged parameter is true,
	 * the has 
	 * @param indexA
	 * @param indexB
	 * @return the constrain from indexA to indexB that is stored in the constraints matrix
	 */
	private void internalSetConstraint(int indexA, int indexB, AbstractConstraint<T> constraintAB, boolean hasChanged) {
		if (indexA < indexB) internalSetConstraint(indexB, indexA, constraintAB.reverse(), hasChanged);
		else if (indexA == indexB) {
			if (constraintAB.equals(constraintsCategory.getEqualConstraint())) return;
			else throw new RuntimeException("internalSetConstraint has just asserted that v"+indexA+" "+constraintAB.inline()+" v"+indexB);
		}
		else {
			this.constraints[indexA-1][indexB] = constraintAB;
			this.hasChanged[indexA-1][indexB] = hasChanged;
		}
	}
	
	/**
	 * Places the desired constraint into the revision bits for the given relationship in 
	 * the adjacency matrix. Returns false if the new constrain produces a contradiction,
	 * I.E. the value of the new revision bits is 0 (FAIL). Otherwise, returns true.
	 * Regardless of the return value, the revision bits will be modified.
	 */
	private boolean internalAddSingleConstraint(int indexA, int indexB, AbstractConstraint<T> constraintAB, boolean addUpdateFlag) {
		AbstractConstraint<T> original = internalGetConstraint(indexA, indexB);
		AbstractConstraint<T> modified = original.and(constraintAB);
		
		boolean updated = (!modified.equals(original) && addUpdateFlag);
		internalSetConstraint(indexA, indexB, modified, updated);
		
		return modified.isPossible();
	}
	
	private boolean internalPropagateConstraint(int indexB, int indexC, boolean doReverse) {
		boolean success = true;
		AbstractConstraint<T> constraintBC = internalGetConstraint(indexB, indexC);
		for (int indexA = 0; indexA < numItems; indexA++) {
			if (indexA == indexB || indexB == indexC || indexA == indexC) continue;
			AbstractConstraint<T> constraintAB = internalGetConstraint(indexA, indexB);
			
			AbstractConstraint<T> implicationAC = constraintAB.chain(constraintBC);
			if (implicationAC.isGuaranteed()) continue;

//			System.out.println("v"+indexA+" "+constraintAB.symbol()+" v"+indexB+" AND "+
//					"v"+indexB+" "+constraintBC.symbol()+" v"+indexC+" IMPLIES "+
//					"v"+indexA+" "+implicationAC.symbol()+" v"+indexC);
			success &= internalAddSingleConstraint(indexA, indexC, implicationAC, true);
		}
		
		if (doReverse) success &= internalPropagateConstraint(indexC, indexB, false);
		
		return success;
	}
	
	private boolean internalAddConstraint(int indexA, int indexB, AbstractConstraint<T> constraintAB) {
		boolean success = true;
		
//		System.out.println("Adding constraint: v"+indexA+" "+constraintAB.symbol()+" v"+indexB);
		
		success &= internalAddSingleConstraint(indexA, indexB, constraintAB, true);
		if (!success) return false;
		
		boolean changes = true;
		while (changes) {
			changes = false;
			for (int i = 1; i <= numItems-1; i++) {
				for (int j = 0; j < i; j++) {
					if (!hasChanged[i-1][j]) continue;
					
					hasChanged[i-1][j] = false;
					changes = true;
					
					success &= internalPropagateConstraint(i, j, true);
					if (!success) return false;
				}
			}
		}
		
		return success;
	}
	
	public boolean addConstraint(int indexA, AbstractConstraint<T> constraint, int indexB) {
		if (constraint.getCategory() != constraintsCategory) throw new IllegalArgumentException("Constraint object's category does not match this ConstraintSolver's category");
		
		if (indexA < 0 || indexA >= numItems) throw new IndexOutOfBoundsException("IndexA = "+indexA+" (numItems = "+numItems+")");
		if (indexB < 0 || indexB >= numItems) throw new IndexOutOfBoundsException("IndexB = "+indexB+" (numItems = "+numItems+")");
		
		createBackup();
		boolean success = internalAddConstraint(indexA, indexB, constraint);
		if (!success) restoreBackup();
		
		return success;
	}

	private void createBackup() {
		for (int i = 1; i <= numItems-1; i++) System.arraycopy(constraints, 0, saveState, 0, i);
	}
	
	private void restoreBackup() {
		for (int i = 1; i <= numItems-1; i++) System.arraycopy(saveState, 0, constraints, 0, i);
	}

	public AbstractConstraint<T> getConstraint(int indexA, int indexB) {
		return internalGetConstraint(indexA, indexB);
	}
	
	public void print() {
		int charsPerNum = (int) Math.ceil(Math.log10(numItems)) + 1;
		
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
				String value = pad+getConstraint(i, j).inline();
				value = value.substring(value.length() - charsPerNum);
				System.out.print(" "+value);
			}
			
			System.out.println();
		}
	}
	
}
