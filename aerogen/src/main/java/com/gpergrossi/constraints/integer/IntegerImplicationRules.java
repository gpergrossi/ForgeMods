package com.gpergrossi.constraints.integer;

import com.gpergrossi.constraints.matrix.ConstraintMatrix;
import com.gpergrossi.constraints.matrix.ImplicationRules;
import com.gpergrossi.constraints.matrix.MatrixEntry;

public class IntegerImplicationRules extends ImplicationRules<IntegerConstraint> {

	public IntegerImplicationRules(ConstraintMatrix<IntegerConstraint> matrix) {
		super(matrix);
	}

	@Override
	public boolean doImplicationUpdates() {		
		MatrixEntry<IntegerConstraint> entry;
		while ((entry = this.poll()) != null) {
			if (entry.getConstraint().isComplex()) throw new UnsupportedOperationException("The IntegerConstraint system does not support complex constraints");
			final boolean possible = propagateConstraint(entry);
			if (!possible) return false;
		}
		return true;
	}

	private boolean propagateConstraint(MatrixEntry<IntegerConstraint> entry) {
		boolean possible = internalPropagateConstraint(entry);
		if (!possible) return possible;
		possible = internalPropagateConstraint(entry.reverse());
		return possible;
	}
	
	private boolean internalPropagateConstraint(MatrixEntry<IntegerConstraint> entryAB) {
		int indexA = entryAB.getIndexA();
		int indexB = entryAB.getIndexB();
		
		for (int indexC = 0; indexC < matrix.size(); indexC++) {
			if (indexA == indexC || indexB == indexC) continue;
			MatrixEntry<IntegerConstraint> entryBC = matrix.getMatrixEntry(indexB, indexC);
			
			IntegerConstraint implicationAC = entryAB.getConstraint().chain(entryBC.getConstraint());
			if (implicationAC.isGuaranteed()) continue;
			
			//System.out.println(entryAB+"  AND  "+entryBC+"  IMPLIES  [v"+indexA+implicationAC.inline()+"v"+indexC+"]");
			
			boolean possible = matrix.andConstraint(indexA, implicationAC, indexC);
			if (!possible) return false;
		}
		
		return true;
	}

}
