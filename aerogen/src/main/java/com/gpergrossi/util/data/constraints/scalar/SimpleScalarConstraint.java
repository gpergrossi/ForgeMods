package com.gpergrossi.util.data.constraints.scalar;

import java.util.HashMap;
import java.util.Map;

import com.gpergrossi.util.data.OrderedPair;
import com.gpergrossi.util.data.constraints.IConstraint;
import com.gpergrossi.util.data.constraints.generic.AbstractConstraint;

public class SimpleScalarConstraint<T extends Comparable<T>> extends AbstractConstraint<T> {
	SimpleScalarConstraints<T> constraints;
	
	public final String symbol;
	public final boolean allowLess;
	public final boolean allowEqual;
	public final boolean allowGreater;
	private Map<OrderedPair<SimpleScalarConstraint<T>>, SimpleScalarConstraint<T>> implicationTable;
	
	public SimpleScalarConstraint(SimpleScalarConstraints<T> constraints, String symbol, boolean allowLess, boolean allowEqual, boolean allowGreater) {
		super(constraints);
		this.constraints = constraints;
		this.symbol = symbol;
		this.allowLess = allowLess;
		this.allowEqual = allowEqual;
		this.allowGreater = allowGreater;
		
		this.implicationTable = null;
	}

	private Map<OrderedPair<SimpleScalarConstraint<T>>, SimpleScalarConstraint<T>> buildImplicationTable() {
		Map<OrderedPair<SimpleScalarConstraint<T>>, SimpleScalarConstraint<T>> implications = new HashMap<>();
		
		// Implies A < C
		implications.put(new OrderedPair<>(constraints.LESS, constraints.LESS), constraints.LESS);
		implications.put(new OrderedPair<>(constraints.LESS, constraints.EQUAL), constraints.LESS);
		implications.put(new OrderedPair<>(constraints.EQUAL, constraints.LESS), constraints.LESS);
		implications.put(new OrderedPair<>(constraints.LESS, constraints.LESS_OR_EQUAL), constraints.LESS);
		implications.put(new OrderedPair<>(constraints.LESS_OR_EQUAL, constraints.LESS), constraints.LESS);
		
		// Implies A <= C
		implications.put(new OrderedPair<>(constraints.EQUAL, constraints.LESS_OR_EQUAL), constraints.LESS_OR_EQUAL);
		implications.put(new OrderedPair<>(constraints.LESS_OR_EQUAL, constraints.EQUAL), constraints.LESS_OR_EQUAL);
		implications.put(new OrderedPair<>(constraints.LESS_OR_EQUAL, constraints.LESS_OR_EQUAL), constraints.LESS_OR_EQUAL);
		
		// Implies A == C
		implications.put(new OrderedPair<>(constraints.EQUAL, constraints.EQUAL), constraints.EQUAL);
		
		// Implies A >= C
		implications.put(new OrderedPair<>(constraints.EQUAL, constraints.GREATER_OR_EQUAL), constraints.GREATER_OR_EQUAL);
		implications.put(new OrderedPair<>(constraints.GREATER_OR_EQUAL, constraints.EQUAL), constraints.GREATER_OR_EQUAL);
		implications.put(new OrderedPair<>(constraints.GREATER_OR_EQUAL, constraints.GREATER_OR_EQUAL), constraints.GREATER_OR_EQUAL);
		
		// Implies A > C
		implications.put(new OrderedPair<>(constraints.GREATER, constraints.GREATER), constraints.GREATER);
		implications.put(new OrderedPair<>(constraints.GREATER, constraints.EQUAL), constraints.GREATER);
		implications.put(new OrderedPair<>(constraints.EQUAL, constraints.GREATER), constraints.GREATER);
		implications.put(new OrderedPair<>(constraints.GREATER, constraints.GREATER_OR_EQUAL), constraints.GREATER);
		implications.put(new OrderedPair<>(constraints.GREATER_OR_EQUAL, constraints.GREATER), constraints.GREATER);
		
		// Implies A != C
		implications.put(new OrderedPair<>(constraints.EQUAL, constraints.NOT_EQUAL), constraints.NOT_EQUAL);
		implications.put(new OrderedPair<>(constraints.NOT_EQUAL, constraints.EQUAL), constraints.NOT_EQUAL);
		
		return implications;
	}

	@Override
	public IConstraint<T> reverse() { 
		return constraints.getConstraint(allowGreater, allowEqual, allowLess);
	}

	@Override
	public boolean check(T valueA, T valueB) {
		if (allowLess && allowEqual && allowGreater) return true;
		if (!allowLess && !allowEqual && !allowGreater) return false;
		
		int result = valueA.compareTo(valueB);
		if (allowLess && result < 0) return true;
		if (allowGreater && result > 0) return true;
		if (allowEqual && result == 0) return true;
		return false;
	}

	@Override
	public String symbol() { 
		return symbol; 
	}
	
	@Override
	public boolean isPossible() { return (allowLess || allowEqual || allowGreater); }
	
	public SimpleScalarConstraint<T> and(SimpleScalarConstraint<T> other) {
		return constraints.getConstraint(this.allowLess && other.allowLess, this.allowEqual && other.allowEqual, this.allowGreater && other.allowGreater);
	}
	
	public SimpleScalarConstraint<T> or(SimpleScalarConstraint<T> other) {
		return constraints.getConstraint(this.allowLess || other.allowLess, this.allowEqual || other.allowEqual, this.allowGreater || other.allowGreater);
	}
	
	public SimpleScalarConstraint<T> getImplication(SimpleScalarConstraint<T> bc) {
		if (implicationTable == null) implicationTable = buildImplicationTable();		
		SimpleScalarConstraint<T> result = implicationTable.get(new OrderedPair<SimpleScalarConstraint<T>>(this, bc));
		if (result == null) result = constraints.PASS;
		return result;
	}
	
}
