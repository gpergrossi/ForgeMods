package com.gpergrossi.util.data.constraints.integer;

import com.gpergrossi.util.data.constraints.generic.AbstractConstraint;

public class IntegerConstraint extends AbstractConstraint<Integer> {

	public static class Category extends AbstractConstraint.Category<Integer> {
		public static Category INSTANCE = new Category();
		
		@Override
		public AbstractConstraint<Integer> getAlwaysConstraint() {
			return IntegerConstraint.ALWAYS;
		}

		@Override
		public AbstractConstraint<Integer> getEqualConstraint() {
			return IntegerConstraint.EQUAL;
		}

		@Override
		public AbstractConstraint<Integer> getNeverConstraint() {
			return IntegerConstraint.NEVER;
		}
	}
	
	public static final IntegerConstraint ALWAYS = new IntegerConstraint(Integer.MIN_VALUE, Integer.MAX_VALUE);
	public static final IntegerConstraint EQUAL = new IntegerConstraint(0, 0);
	public static final IntegerConstraint NEVER = new IntegerConstraint(1, -1);
	
	protected final int relativeMin, relativeMax;
	
	protected IntegerConstraint(int relativeMin, int relativeMax) {
		super(Category.INSTANCE);
		this.relativeMin = relativeMin;
		this.relativeMax = relativeMax;
	}

	@Override
	public AbstractConstraint<Integer> reverse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractConstraint<Integer> compliment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractConstraint<Integer> and(AbstractConstraint<Integer> other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractConstraint<Integer> or(AbstractConstraint<Integer> other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractConstraint<Integer> chain(AbstractConstraint<Integer> bc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPossible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isGuaranteed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean check(Integer valueA, Integer valueB) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean equals(AbstractConstraint<Integer> other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String inline() {
		// TODO Auto-generated method stub
		return null;
	}

}
