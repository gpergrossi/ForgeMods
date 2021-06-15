package com.gpergrossi.constraints.integer;

import java.util.Random;

import com.gpergrossi.constraints.matrix.ConstraintMatrix;
import com.gpergrossi.constraints.matrix.MatrixEntry;
import com.gpergrossi.util.data.WeightedList;

public class IntegerSolver {
	
	private Random random;
	
	public IntegerSolver(Random random) {
		this.random = random;
	}
	
	public ConstraintMatrix<IntegerConstraint> solve(ConstraintMatrix<IntegerConstraint> matrix) {
		matrix = matrix.copy();
		
		while (true) {
			WeightedList<MatrixEntry<IntegerConstraint>> entries = new WeightedList<>();
			
			for (int a = 0; a < matrix.size; a++) {
				for (int b = 0; b < a; b++) {
					final MatrixEntry<IntegerConstraint> entry = matrix.getMatrixEntry(a, b);
					final int numValues = entry.getConstraint().getNumValues();
					if (numValues == 1) continue;
					
					final double numValuesD = numValues;
					int weight = Math.max((int) Math.ceil(20000.0*(1.0 - numValuesD/(numValuesD+1.0))), 1);
					entries.add(entry, weight);
				}
			}
			
			if (entries.size() == 0) return matrix;
			
			final MatrixEntry<IntegerConstraint> entry = entries.getRandom(random);
			final int value = entry.getConstraint().getRandomValue(random);

			//System.out.println("Assigning [v"+entry.getIndexA()+" = "+value+" + v"+entry.getIndexB()+"]");
			final boolean success = entry.andConstraint(IntegerConstraint.equal(value));
			//if (success) matrix.print();
			
			// TODO This is based on the assumption that simple (i.e. single-range) integer constraints
			// cannot produces sudoku-like possiblities. In sudoku, the individual cells may have multiple
			// potential values that do not contradict existing solved cells. But, when a value is tried, it
			// may propagate and reveal that it is not valid. The simple constraint limitation for Integer
			// constraints should make this impossible. Thus, if an entry is reduced to a single valid value
			// it should always succeed.
			if (!success) {
				throw new RuntimeException("Severe problem while solving. Matrix=\n"+matrix.toString()+"\n\nCannot set entry "+entry+" to [="+value+"]");
			}
		}
	}
	
}
