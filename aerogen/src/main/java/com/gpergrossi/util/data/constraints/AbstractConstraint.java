package com.gpergrossi.util.data.constraints;

/**
 * <i>All classes that extend AbstractConstraint should be immutable!</i><br /><br />
 * 
 * A {@code Constraint} describes the relationship between two variables. The operators &lt;, &gt;, and =
 * are examples of constraints. They describe a required relationship between their left-hand argument
 * and their right-hand argument in order for them to return true. <br /><br />
 * 
 * Constraints must also be operable via the {@link #and}, {@link #or}, and {@link #chain} methods, which are 
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
 * assertions and therefore to detect impossible asserts being made. If 'A < B' is already stored in the
 * constraint solver and 'B < A' is asserted, the solver would AND these two assertions together and get
 * the NEVER constraint, indicating that the new assertion, 'B < A', is invalid.
 *
 * @param <Subclass> the constraint subclass type
 * @param <Type> the value type
 */
public abstract class AbstractConstraint<Subclass extends AbstractConstraint<Subclass, Type>, Type> {
	
	/**
	 * An {@code AbstractConstraintCategory} groups {@code AbstractConstraint}s that
	 * are safe to operate with each other. Constraints in a Category will have well-defined
	 * {@link AbstractConstrain#and and}, {@link AbstractConstrain#or or}, and {@link AbstractConstrain#chain chain}
	 * results with other Constraints from the same Category.<br /><br />
	 * 
	 * ConstraintCategory makes the primitive Constraints, ALWAYS, EQUAL, and NEVER, publicly available.<br /><br />
	 * 
	 * All constraints must be constructed with a ConstraintCategory as their parent.
	 * For most basic implementations of ConstraintCategory, it is recommended to use the Singleton pattern
	 * and automatically provide the Singleton instance to Constraints being constructed in that Category.<br />
	 * ConstraintCategories are then used in the construction of ConstraintSolvers.<br />
	 *
	 * @param <S> the constraint subclass type
	 * @param <T> the value type
	 */
	public static abstract class Category<Subclass extends AbstractConstraint<Subclass, ?>> {
		public abstract Subclass getAlwaysConstraint();
		public abstract Subclass getEqualConstraint();
		public abstract Subclass getNeverConstraint();
	}

	protected final Category<Subclass> category;
	
	public AbstractConstraint(Category<Subclass> category) {
		this.category = category;
	}
	
	public Category<Subclass> getCategory() {
		return category;
	}
	
	/**
	 * Returns the constraint representing the reverse
	 * of this constraint's relationship. If this constraint
	 * is the relation from A to B, returns the relation from B
	 * to A. (E.G. LESS.reverse() == GREATER)
	 */
	public abstract Subclass reverse();

	/**
	 * Returns the constraint representing the compliment (inverse)
	 * of this constraint's relationship. <br />
	 * For any constraint, one can imagine a set of values 'B' wherein the constraint returns true for a constant value 'A'. <br />
	 * The compliment constraint would give a set of values 'B-prime' that is the compliment of the normal set 'B'. 
	 * That is, all values in 'B-prime' fail the constraint for a given constant value 'A'. <br />
	 * An example of this would be LESS.compliment() returns GREATER_OR_EQUAL.
	 */
	public abstract Subclass compliment();
	
	/**
	 * Returns the constraint representing this constraint AND {@code other}.
	 */
	public abstract Subclass and(Subclass other);
	
	/**
	 * Returns the constraint representing this constraint OR {@code other}
	 */
	public abstract Subclass or(Subclass other);
	
	/**
	 * Returns the implied constraint from A to C, treating this constraint as the relation from A to B
	 * and using the parameter {@code bc} as the relation from B to C.<br />
	 * An example of this would be LESS.chain(EQUAL) returns LESS (E.G. A &lt; B and B = C implies A &lt; C)
	 * @param bc - relation from B to C. For example {@code bc} could be 'LESS', meaning B is LESS than C.
	 * @return the implied relation from A to C, or null if there is no implied relationship
	 */
	public abstract Subclass chain(Subclass bc);

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
	public abstract boolean isPossible();
	
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
	public abstract boolean isGuaranteed();
	
	/**
	 * Return true if the constraint allows the given values {@code valueA} and {@code valueB}.
	 * @param valueA the value on the 'left' of the constraint operation
	 * @param valueB the value on the 'right' of the constraint operation
	 * @return true if {@code valueA} &lt;constraint&gt; {@code valueB}. (E.G. A < B)
	 */
	public abstract boolean check(Type valueA, Type valueB);
	
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
	public abstract boolean equals(Subclass other);
	
	/**
	 * Return a short inline string to be placed between variables when printing this constraint.
	 * For simple constraints this may just be an operator such as "<", but for constraints like
	 * "A is less than (B minus 5)" there needs to be a more creative inline notation, such as:
	 * "A <b>+5<</b> B"
	 * @return
	 */
	public abstract String inline();
	
}
