package com.gpergrossi.util.data.constraints.generic;

import com.gpergrossi.util.data.constraints.IConstraint;

public abstract class AbstractConstraint<T> implements IConstraint<T> {

	protected Constraints<T> constraints;
	
	public AbstractConstraint(Constraints<T> constraints) {
		this.constraints = constraints;
	}

	@Override
	public IConstraint<T> and(IConstraint<T> other) {
		return constraints.and(this, other);
	}

	@Override
	public IConstraint<T> or(IConstraint<T> other) {
		return constraints.or(this, other);
	}
	
	@Override
	public IConstraint<T> getImplication(IConstraint<T> bc) {
		return constraints.getImplication(this, bc);
	}

	@Override
	public boolean isPossible() {
		return true;
	}

	@Override
	public boolean equals(IConstraint<T> other) {
		return this == other;
	}
	
}
