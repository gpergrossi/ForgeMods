package com.gpergrossi.util.data.constraints.scalar;

import java.util.HashMap;
import java.util.Map;

import com.gpergrossi.util.data.constraints.IConstraint;
import com.gpergrossi.util.data.constraints.generic.Constraints;
import com.gpergrossi.util.data.constraints.generic.IConstraintInteroperations;

public class SimpleScalarConstraints<T extends Comparable<T>> extends Constraints<T> {

	private static Map<Class<?>, SimpleScalarConstraints<?>> storedResults = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> SimpleScalarConstraints<T> forType(Class<T> clazz) {
		Constraints<?> result = storedResults.get(clazz);
		if (result == null) {
			result = new SimpleScalarConstraints<T>();
			storedResults.put(clazz, (SimpleScalarConstraints<?>) result);
		}
		return (SimpleScalarConstraints<T>) result;
	}
	
	public final SimpleScalarConstraint<T> IMPOSSIBLE;
	public final SimpleScalarConstraint<T> LESS;
	public final SimpleScalarConstraint<T> LESS_OR_EQUAL;
	public final SimpleScalarConstraint<T> EQUAL;
	public final SimpleScalarConstraint<T> GREATER_OR_EQUAL;
	public final SimpleScalarConstraint<T> GREATER;
	public final SimpleScalarConstraint<T> NOT_EQUAL;
	public final SimpleScalarConstraint<T> PASS;
	
	protected SimpleScalarConstraints() {
		super();
		
		IMPOSSIBLE = new SimpleScalarConstraint<>(this, "XX", false, false, false);
		LESS = new SimpleScalarConstraint<>(this, "<", true, false, false);
		LESS_OR_EQUAL = new SimpleScalarConstraint<>(this, "<=", true, true, false);
		EQUAL = new SimpleScalarConstraint<>(this, "==", false, true, false);
		GREATER_OR_EQUAL = new SimpleScalarConstraint<>(this, ">=", false, true, true);
		GREATER = new SimpleScalarConstraint<>(this, ">", false, false, true);
		NOT_EQUAL = new SimpleScalarConstraint<>(this, "!=", true, false, true);
		PASS = new SimpleScalarConstraint<>(this, "", true, true, true);

		addOperation(SimpleScalarConstraint.class, SimpleScalarConstraint.class, createInteroperations());
	}
	
	private IConstraintInteroperations<T> createInteroperations() {
		return new IConstraintInteroperations<T>() {
			@Override
			public IConstraint<T> and(IConstraint<T> constraint1, IConstraint<T> constraint2) {
				SimpleScalarConstraint<T> sc1 = (SimpleScalarConstraint<T>) constraint1;
				SimpleScalarConstraint<T> sc2 = (SimpleScalarConstraint<T>) constraint2;
				return sc1.and(sc2);
			}

			@Override
			public IConstraint<T> or(IConstraint<T> constraint1, IConstraint<T> constraint2) {
				SimpleScalarConstraint<T> sc1 = (SimpleScalarConstraint<T>) constraint1;
				SimpleScalarConstraint<T> sc2 = (SimpleScalarConstraint<T>) constraint2;
				return sc1.or(sc2);
			}

			@Override
			public IConstraint<T> getImplication(IConstraint<T> constraintAB, IConstraint<T> constraintBC) {
				SimpleScalarConstraint<T> sc1 = (SimpleScalarConstraint<T>) constraintAB;
				SimpleScalarConstraint<T> sc2 = (SimpleScalarConstraint<T>) constraintBC;
				return sc1.getImplication(sc2);
			}
		};
	}

	public SimpleScalarConstraint<T> getConstraint(boolean allowLess, boolean allowEqual, boolean allowGreater) {
		int index = (allowLess ? 1 : 0) | (allowEqual ? 2 : 0) | (allowGreater ? 4 : 0);
		switch (index) {
			case 0: return IMPOSSIBLE;
			case 1: return LESS;
			case 2: return EQUAL;
			case 3: return LESS_OR_EQUAL;
			case 4:	return GREATER;
			case 5: return NOT_EQUAL;
			case 6: return GREATER_OR_EQUAL;
			case 7: return PASS;
			default: return null;
		}
	}

	@Override
	public IConstraint<T> getEqualConstraint() {
		return EQUAL;
	}

	@Override
	public IConstraint<T> getImpossibleConstraint() {
		return IMPOSSIBLE;
	}

	@Override
	public IConstraint<T> getAlwaysTrueConstraint() {
		return PASS;
	}
	
}
