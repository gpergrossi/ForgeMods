package com.gpergrossi.util.constraints.generic;

/**
 * <i>All classes that implement IConstraint should be immutable!</i><br /><br />
 * 
 * An {@code IConstraint} describes the relationship between two variables. The operators &lt;, &gt;, and =
 * are examples of constraints. They describe a required relationship between their left-hand argument
 * and their right-hand argument. <br /><br />
 * 
 * IConstraints must be operable via the {@link #and}, {@link #or}, and {@link #chain} methods, which are 
 * described in detail in their method-level javadoc comments. The methods in this class provide
 * ways of reasoning about constraints that are necessary for the constraint solver.<br /><br />
 * 
 * Generally, any Constraint object belongs to some category of constraints,
 * for example "Basic Integer Constraints", which might include 
 * LESS, LESS_OR_EQUAL, EQUAL, GREATER, GREATER_OR_EQUAL, and NOT_EQUAL. 
 * Additionally, all constraint categories should have a concept of ALWAYS, and NEVER.
 * These are constraints that are always true or never true, respectively.<br /><br />
 * 
 * The constraints NEVER, EQUAL, and ALWAYS are the special bare-minimum constraints that
 * every category must implement in order to be useful in the constraint solver.<br />
 * 
 * ALWAYS is the initial relationship between any two distinct variables. It indicates
 * that there is no restriction on the relationship because any two values would pass (return true).<br />
 * 
 * EQUAL is the initial relationship between any variable and itself (I.E. 'A == A'). This
 * constraint is necessary because it raises a contradiction, for example, when 'A < A' is asserted.<br />
 * 
 * NEVER is the last mandatory constraint. It is used by the constraint solver to mark any contradicting
 * assertions and therefore to detect impossible assertions being made. If 'A < B' is already stored in the
 * constraint solver and 'B < A' is asserted, the solver would AND these two assertions together and get
 * the NEVER constraint, indicating that the new assertion, 'B < A', is invalid.
 *
 * @param <Subclass> the type of the lowest-level IConstraint subclass
 * @param <Type> the value type for the constraint (e.g. Integer)
 */
public interface IConstraint<Subclass extends IConstraint<Subclass, Type>, Type> {
	
	public IConstraintClass<Subclass> getConstraintClass();
	
	/**
	 * Returns the constraint representing the reverse
	 * of this constraint's relationship. If this constraint
	 * is the relation from A to B, returns the relation from B
	 * to A. (E.G. LESS.reverse() == GREATER)
	 */
	public Subclass reverse();
	
	/**
	 * Returns the constraint representing this constraint AND {@code other}.
	 */
	public Subclass and(Subclass other);

	/**
	 * Returns true if there is any possible pair of values that might satisfy this
	 * constraint. The isPossible() method exists so that "always false" constraints can
	 * can easily be checked. Such a constraint might be returned by the and() method, for example,
	 * when LESS.and(GREATER) is called. There should be no possible pair of values to satisfy
	 * both LESS and GREATER, thus a NEVER constraint would be returned whose isPossible()
	 * method returns false.<br /><br />
	 * 
	 * <b>This method is necessary for constraint propagation and must be implemented correctly for
	 * all Constraint classes.</b>
	 * @return
	 */
	public boolean isPossible();
	
	/**
	 * Returns true if there is no possible pair of values for which this constraint
	 * would fail. The isGuaranteed() method exists so that "always true" constraints can
	 * easily be checked. Such a constraint might be returned by the or() method, for example,
	 * when LESS_OR_EQUAL.or(GREATER) is called. Any pair of values should satisfy either
	 * the LESS_OR_EQAUL constraint or the GREATER constraint, thus an ALWAYS constraint 
	 * would be returned whose isGuaranteed() method returns true.<br /><br />
	 * 
	 * <b>This method is necessary for constraint propagation and must be implemented correctly for
	 * all Constraint classes.</b>
	 * @return
	 */
	public boolean isGuaranteed();
	
	/**
	 * Return true if the constraint allows the given values {@code valueA} and {@code valueB}.
	 * @param valueA the value on the 'left' of the constraint operation
	 * @param valueB the value on the 'right' of the constraint operation
	 * @return true if {@code valueA} &lt;constraint&gt; {@code valueB}. (E.G. A < B)
	 */
	public boolean check(Type a, Type b);
	
	/**
	 * Return true if this constraint and {@code other} represent the same concept.
	 * For example, any constraint whose isGuaranteed() method returns true must 
	 * return true for equals(ALWAYS).<br /><br />
	 * 
	 * <b>This method is necessary for constraint propagation and must be implemented correctly for
	 * all Constraint classes.</b>
	 * @param other
	 * @return
	 */
	public boolean equals(Subclass other);
	
	/**
	 * Return a short inline string to be placed between variables when printing this constraint.
	 * For simple constraints this may just be an operator such as "<", but for constraints like
	 * "A is less than (B minus 5)" there needs to be a more creative inline notation, such as:
	 * "A <b>+5<</b> B"
	 * @return
	 */
	public String inline();
	
}
