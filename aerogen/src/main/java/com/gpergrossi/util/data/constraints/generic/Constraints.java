package com.gpergrossi.util.data.constraints.generic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.gpergrossi.util.data.OrderedPair;
import com.gpergrossi.util.data.constraints.IConstraint;

public abstract class Constraints<T> {
	
	protected Set<IConstraint<T>> values;
	protected Map<OrderedPair<Class<?>>, IConstraintInteroperations<T>> interoperations;
	
	protected Constraints() {
		values = new HashSet<>();
		interoperations = new HashMap<>();
	}
	
	public Iterable<IConstraint<T>> values() {
		return new Iterable<IConstraint<T>>() {
			public Iterator<IConstraint<T>> iterator() {
				return values.iterator();
			}
		};
	}
	
	public abstract IConstraint<T> getEqualConstraint();
	public abstract IConstraint<T> getImpossibleConstraint();
	public abstract IConstraint<T> getAlwaysTrueConstraint();
	
	public IConstraint<T> and(IConstraint<T> a, IConstraint<T> b) {
		OrderedPair<Class<?>> lookup = new OrderedPair<>(a.getClass(), b.getClass());
		
		IConstraintInteroperations<T> operation = interoperations.get(lookup);
		if (operation != null) {
			return operation.and(a, b);
		} else {
			operation = interoperations.get(lookup.reverse());
			if (operation == null) throw new UnsupportedOperationException();
			return operation.and(b, a);
		}
	}
	
	public IConstraint<T> or(IConstraint<T> a, IConstraint<T> b) {
		OrderedPair<Class<?>> mapKey = new OrderedPair<>(a.getClass(), b.getClass());
		
		IConstraintInteroperations<T> operation = interoperations.get(mapKey);
		if (operation != null) {
			return operation.or(a, b);
		} else {
			operation = interoperations.get(mapKey.reverse());
			if (operation == null) throw new UnsupportedOperationException();
			return operation.or(b, a);
		}
	}
	
	public IConstraint<T> getImplication(IConstraint<T> ab, IConstraint<T> bc) {
		OrderedPair<Class<?>> mapKey = new OrderedPair<>(ab.getClass(), bc.getClass());
		
		IConstraintInteroperations<T> operation = interoperations.get(mapKey);
		if (operation != null) {
			return operation.getImplication(ab, bc);
		} else {
			operation = interoperations.get(mapKey.reverse());
			if (operation == null) throw new UnsupportedOperationException();
			return operation.getImplication(bc.reverse(), ab.reverse()).reverse();
		}
	}
	
	public boolean addOperation(Class<?> classA, Class<?> classB, IConstraintInteroperations<T> operations) {
		OrderedPair<Class<?>> mapKey = new OrderedPair<>(classA, classB);
		if (interoperations.containsKey(mapKey)) return false;		
		interoperations.put(mapKey, operations);
		return true;
	}
	
}
