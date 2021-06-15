package com.gpergrossi.constraints.generic;

import com.gpergrossi.constraints.matrix.ConstraintMatrix;
import com.gpergrossi.constraints.matrix.ImplicationRules;

/**
 * An {@code IConstraintClass} represents a class of constraints (e.g. Integer constraints).
 * The IConstraintClass object unites all IConstraints that return it, guaranteeing that they are safe
 * to operate with each other. IConstraints of a particular IConstraintClass will have well-defined 
 * {@link AbstractConstrain#and and}, {@link AbstractConstrain#or or}, and {@link AbstractConstrain#chain chain}
 * results with other IConstraints from the same IConstraintClass.<br /><br />
 * 
 * IConstraintClass makes the primitive Constraints, ALWAYS, EQUAL, and NEVER, publicly available.<br /><br />
 *
 * @param <Subclass> the type of the lowest-level IConstraint subclass
 */
public interface IConstraintClass<Subclass extends IConstraint<Subclass, ?>> {
	
	public Subclass getAlwaysConstraint();
	public Subclass getEqualConstraint();
	public Subclass getNeverConstraint();
	
	public ImplicationRules<Subclass> getImplicationRules(ConstraintMatrix<Subclass> matrix);
	
}
