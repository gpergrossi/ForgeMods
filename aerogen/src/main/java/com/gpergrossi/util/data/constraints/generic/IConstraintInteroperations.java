package com.gpergrossi.util.data.constraints.generic;

import com.gpergrossi.util.data.constraints.IConstraint;

public interface IConstraintInteroperations<T> {

	public IConstraint<T> and(IConstraint<T> constraint1, IConstraint<T> constraint2);

	public IConstraint<T> or(IConstraint<T> constraint1, IConstraint<T> constraint2);
	
	public IConstraint<T> getImplication(IConstraint<T> constraintAB, IConstraint<T> constraintBC);
	
}
