package com.gpergrossi.aerogen.generator.regions;

import java.util.Random;

import com.gpergrossi.util.constraints.integer.IntegerConstraint;
import com.gpergrossi.util.constraints.matrix.ConstraintMatrix;
import com.gpergrossi.util.constraints.matrix.MatrixEntry;
import com.gpergrossi.util.data.WeightedList;
import com.gpergrossi.util.data.ranges.Int1DMultiRange;

public class AltitudeSolver {

	private Region region;
	private Random random;
	
	public AltitudeSolver(Region region, Random random) {
		this.region = region;
		this.random = random;
	}
	
	public ConstraintMatrix<IntegerConstraint> solve(ConstraintMatrix<IntegerConstraint> matrix) {
		matrix = matrix.copy();
		
		while (true) {
			WeightedList<MatrixEntry<IntegerConstraint>> entries = new WeightedList<>();
			
			for (int a = 1; a < matrix.size; a++) {
				final MatrixEntry<IntegerConstraint> entry = matrix.getMatrixEntry(a, 0);
				final int numValues = entry.getConstraint().getNumValues();
				if (numValues == 1) continue;
				
				final double numValuesD = numValues;
				int weight = Math.max((int) Math.ceil(20000.0*(1.0 - numValuesD/(numValuesD+1.0))), 1);
				entries.add(entry, weight);
			}
			
			if (entries.size() == 0) return matrix;
			
			final MatrixEntry<IntegerConstraint> entry = entries.getRandom(random);
			
			final Int1DMultiRange validValues = entry.getConstraint().getValidValues();
			final int min = validValues.valueFor(0);
			final int max = validValues.valueFor(validValues.size()-1);
			
			final int value = region.getBiome().getRandomIslandAltitude(random, min, max);

			//System.out.println("Assigning [v"+entry.getIndexA()+" = "+value+" + v"+entry.getIndexB()+"]");
			final boolean success = entry.andConstraint(IntegerConstraint.equal(value));
			
			if (!success) {
				throw new RuntimeException("Severe problem while solving altitudes. Matrix=\n"+matrix.toString()
				+"\n\nCannot assign [v"+entry.getIndexA()+" = "+value+" + v"+entry.getIndexB()+"]");
			}
		}
	}
	
}
