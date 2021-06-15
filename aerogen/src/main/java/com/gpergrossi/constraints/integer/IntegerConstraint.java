package com.gpergrossi.constraints.integer;

import java.util.Random;

import com.gpergrossi.constraints.generic.IConstraint;
import com.gpergrossi.constraints.generic.IConstraintClass;
import com.gpergrossi.constraints.matrix.ConstraintMatrix;
import com.gpergrossi.constraints.matrix.ImplicationRules;
import com.gpergrossi.util.geom.ranges.Int1DMultiRange;
import com.gpergrossi.util.geom.ranges.Int1DRange;

public class IntegerConstraint implements IConstraint<IntegerConstraint, Integer> {

	public static final IConstraintClass<IntegerConstraint> CLASS = Class.INSTANCE;
	
	private static class Class implements IConstraintClass<IntegerConstraint> {
		public static IConstraintClass<IntegerConstraint> INSTANCE = new Class();
		
		private Class() {}
		
		@Override
		public IntegerConstraint getAlwaysConstraint() {
			return IntegerConstraint.ALWAYS;
		}

		@Override
		public IntegerConstraint getEqualConstraint() {
			return IntegerConstraint.EQUAL;
		}

		@Override
		public IntegerConstraint getNeverConstraint() {
			return IntegerConstraint.NEVER;
		}
		
		@Override
		public ImplicationRules<IntegerConstraint> getImplicationRules(ConstraintMatrix<IntegerConstraint> matrix) {
			return new IntegerImplicationRules(matrix);
		}
	}
	
	public static final IntegerConstraint NEVER = new IntegerConstraint(Int1DRange.EMPTY);
	public static final IntegerConstraint LESS = new IntegerConstraint(Integer.MIN_VALUE, -1);
	public static final IntegerConstraint LESS_OR_EQUAL = new IntegerConstraint(Integer.MIN_VALUE, 0);
	public static final IntegerConstraint EQUAL = new IntegerConstraint(0, 0);
	public static final IntegerConstraint GREATER_OR_EQUAL = new IntegerConstraint(0, Integer.MAX_VALUE);
	public static final IntegerConstraint GREATER = new IntegerConstraint(1, Integer.MAX_VALUE);
	public static final IntegerConstraint ALWAYS = new IntegerConstraint(Int1DRange.ALL);
	
	public static IntegerConstraint less(int offset) {
		if (offset == 0) return LESS;
		return new IntegerConstraint(Integer.MIN_VALUE, offset-1);
	}
	
	public static IntegerConstraint lessOrEqual(int offset) {
		if (offset == 0) return LESS_OR_EQUAL;
		return new IntegerConstraint(Integer.MIN_VALUE, offset);
	}
	
	public static IntegerConstraint equal(int offset) {
		if (offset == 0) return EQUAL;
		return new IntegerConstraint(offset, offset);
	}
	
	public static IntegerConstraint greaterOrEqual(int offset) {
		if (offset == 0) return GREATER_OR_EQUAL;
		return new IntegerConstraint(offset, Integer.MAX_VALUE);
	}
	
	public static IntegerConstraint greater(int offset) {
		if (offset == 0) return GREATER;
		return new IntegerConstraint(offset+1, Integer.MAX_VALUE);
	}
	
	
	
	/**
	 * Values that are permitted for 'A minus B' if this constraint is 'from A to B'.
	 * Think of constraints as a directed graph edge. They will have a source and a destination.
	 * For example, "A < B" represents the LESS constraint from A to B. A is less than B, 
	 * so A-B is negative. Therefore the validValues would be  (Integer.MIN_VALUE to -1).
	 */
	private final Int1DMultiRange validValues;
	
	/**
	 * Constructs an integer constraint with the given validValues.
	 * The validValues multi-range is used directly and is not copied;
	 * If the original multi-range is modified, this constraint will
	 * also be affected.
	 * @param validValues - values that are permitted for 'A minus B' if this constraint is from A to B.
	 */
	private IntegerConstraint(Int1DMultiRange validValues) {
		this.validValues = validValues;		
	}

	private IntegerConstraint(Int1DRange range) {
		this(new Int1DMultiRange(range));
	}
	
	private IntegerConstraint(int min, int max) {
		this(new Int1DMultiRange(min, max));
	}
	
	
	
	@Override
	public IConstraintClass<IntegerConstraint> getConstraintClass() {
		return CLASS;
	}
	
	@Override
	public IntegerConstraint reverse() {
		return new IntegerConstraint(this.validValues.reverse());
	}

	@Override
	public IntegerConstraint and(IntegerConstraint other) {
		return new IntegerConstraint(validValues.intersect(other.validValues));
	}

	/**
	 * The chain implication of two IntegerConstraints represented as Int1DMultiRanges 
	 * turns out to be the mathematical convolution of the two Int1DMultiRanges.
	 */
	public IntegerConstraint chain(IntegerConstraint bc) {
		return new IntegerConstraint(validValues.convolve(bc.validValues));
	}

	@Override
	public boolean isPossible() {
		return !this.equals(NEVER);
	}

	@Override
	public boolean isGuaranteed() {
		return this.equals(ALWAYS);
	}

	@Override
	public boolean check(Integer valueA, Integer valueB) {
		return validValues.contains((long) valueA - valueB);
	}
	
	@Override
	public boolean equals(IntegerConstraint other) {
		return this.validValues.equals(other.validValues);
	}

	public boolean isComplex() {
		return (validValues.getRanges().size() > 1);
	}
	
	public int getNumValues() {
		long numValues = validValues.size();
		if (numValues >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
		if (numValues <= Integer.MIN_VALUE) return Integer.MIN_VALUE;
		return (int) numValues;
	}

	public int valueFor(long index) {
		return validValues.valueFor(index);
	}
	
	public int getRandomValue(Random random) {
		return validValues.random(random);
	}

	public Int1DMultiRange getValidValues() {
		return new Int1DMultiRange(validValues);		
	}
	
	@Override
	public String inline() {
		if (this.equals(NEVER)) return " allows no ";
		if (this.equals(LESS)) return " < ";
		if (this.equals(LESS_OR_EQUAL)) return " <= ";
		if (this.equals(EQUAL)) return " == ";
		if (this.equals(GREATER)) return " > ";
		if (this.equals(GREATER_OR_EQUAL)) return " >= ";
		if (this.equals(ALWAYS)) return " allows any ";
		
		if (this.validValues.getRanges().size() == 1) {
			Int1DRange range = this.validValues.asSimpleRange().get();
			
			// LESS or LESS_OR_EQUAL with offset
			if (range.contains(Integer.MIN_VALUE)) return " < "+(range.max+1)+" + ";
			
			// GREATER or GREATER_OR_EQUAL with offset
			if (range.contains(Integer.MAX_VALUE)) return " > "+(range.min-1)+" + ";
			
			// EQUAL with offset
			if (range.size == 1) return " == "+(range.min)+" + ";
		}
		
		// default
		return " == "+validValues+" + ";
	}

}
