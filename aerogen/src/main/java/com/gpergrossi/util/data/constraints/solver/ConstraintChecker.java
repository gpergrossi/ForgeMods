package com.gpergrossi.util.data.constraints.solver;

import com.gpergrossi.util.data.constraints.IConstraint;
import com.gpergrossi.util.data.constraints.generic.Constraints;

public class ConstraintChecker<T> {

	protected final int numItems;
	protected final Constraints<T> constraintsType;
	protected final IConstraint<?>[][] constraints;
	protected final IConstraint<?>[][] saveState;
	protected final boolean[][] hasChanged;
	
	public ConstraintChecker(int numItems, Constraints<T> constraintTypes) {
		this.numItems = numItems;
		this.constraintsType = constraintTypes;
		this.saveState = new IConstraint<?>[numItems-1][];
		this.constraints = new IConstraint<?>[numItems-1][];
		this.hasChanged = new boolean[numItems-1][];
		for (int i = 1; i <= numItems-1; i++) {
			this.saveState[i-1] = new IConstraint<?>[i];
			this.constraints[i-1] = new IConstraint<?>[i];
			this.hasChanged[i-1] = new boolean[i];
			for (int j = 0; j < i; j++) {
				this.saveState[i-1][j] = constraintTypes.getAlwaysTrueConstraint();
				this.constraints[i-1][j] = constraintTypes.getAlwaysTrueConstraint();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private IConstraint<T> internalGetConstraint(int indexA, int indexB) {
		if (indexA < indexB) return internalGetConstraint(indexB, indexA).reverse();
		if (indexA == indexB) return constraintsType.getEqualConstraint();
		return (IConstraint<T>) constraints[indexA-1][indexB];
	}
	
	private void internalSetConstraint(int indexA, int indexB, IConstraint<T> constraintAB, boolean hasChanged) {
		if (indexA < indexB) internalSetConstraint(indexB, indexA, constraintAB.reverse(), hasChanged);
		else if (indexA == indexB) {
			if (constraintAB.equals(constraintsType.getEqualConstraint())) return;
			else throw new RuntimeException("internalSetConstraint has just asserted that v"+indexA+" "+constraintAB.symbol()+" v"+indexB);
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
	private boolean internalAddSingleConstraint(int indexA, int indexB, IConstraint<T> constraintAB, boolean addUpdateFlag) {
		IConstraint<T> original = internalGetConstraint(indexA, indexB);
		IConstraint<T> modified = original.and(constraintAB);
		
		boolean updated = (!modified.equals(original) && addUpdateFlag);
		internalSetConstraint(indexA, indexB, modified, updated);
		
		return modified.isPossible();
	}
	
	private boolean internalPropagateConstraint(int indexB, int indexC, boolean doReverse) {
		boolean success = true;
		IConstraint<T> constraintBC = internalGetConstraint(indexB, indexC);
		for (int indexA = 0; indexA < numItems; indexA++) {
			if (indexA == indexB || indexB == indexC || indexA == indexC) continue;
			IConstraint<T> constraintAB = internalGetConstraint(indexA, indexB);
						
			IConstraint<T> implicationAC = constraintsType.getImplication(constraintAB, constraintBC);
			if (implicationAC.equals(constraintsType.getAlwaysTrueConstraint())) continue;

//			System.out.println("v"+indexA+" "+constraintAB.symbol()+" v"+indexB+" AND "+
//					"v"+indexB+" "+constraintBC.symbol()+" v"+indexC+" IMPLIES "+
//					"v"+indexA+" "+implicationAC.symbol()+" v"+indexC);
			success &= internalAddSingleConstraint(indexA, indexC, implicationAC, true);
		}
		
		if (doReverse) success &= internalPropagateConstraint(indexC, indexB, false);
		
		return success;
	}
	
	private boolean internalAddConstraint(int indexA, int indexB, IConstraint<T> constraintAB) {
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
	
	public boolean addConstraint(int indexA, IConstraint<T> constraint, int indexB) {
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

	public IConstraint<T> getConstraint(int indexA, int indexB) {
		return internalGetConstraint(indexA, indexB);
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
				String value = pad+getConstraint(i, j).symbol();
				value = value.substring(value.length() - charsPerNum);
				System.out.print(" "+value);
			}
			
			System.out.println();
		}
	}
	
}
