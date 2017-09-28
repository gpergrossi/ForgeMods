package com.gpergrossi.util.data.constraints;

/**
 * All classes that implement IConstraint should be immutable
 * @author Gregary
 *
 * @param <T> the value type
 */
public interface IConstraint<T> {
	
	/**
	 * Returns the constraint representing the reverse
	 * of this constraint's relationship. If this constraint
	 * is the relation from A to B, returns the relation from B
	 * to A. (E.G. LESS.reverse() == GREATER)
	 */
	public IConstraint<T> reverse();

	/**
	 * Returns the constraint representing the compliment (inverse)
	 * of this constraint's relationship. Imagine the set of all
	 * B such that this constraint holds given a constant A.
	 * The inverse constraint would give a set B that is the compliment
	 * of the original result. (E.G. LESS.compliment() == GREATER_OR_EQUAL)
	 */
	public IConstraint<T> compliment();
	
	/**
	 * Returns the constraint representing this constraint AND {@code other}.
	 */
	public IConstraint<T> and(IConstraint<T> other);
	
	/**
	 * Returns the constraint representing this constraint OR {@code other}
	 */
	public IConstraint<T> or(IConstraint<T> other);
	
	/**
	 * Returns the implied constraint from A to C, treating this
	 * constraint as the relation from A to B and using the
	 * parameter {@code bc} as the relation from B to C.
	 * @param bc - relation from B to C, I.E. "B is &lt;bc&gt; C"
	 * @return the implied relation from A to C, or null if there is no implied relationship
	 */
	public IConstraint<T> getImplication(IConstraint<T> bc);

	/**
	 * Returns true if there is any possible pair of values that might satisfy this
	 * constraint. The isPossible() method exists so that impossible constraints can
	 * be defined. Such constraints might be returned by the and() method, for example,
	 * when LESS.and(GREATER) is called. In most contexts there would be no possible
	 * pair of values to satisfy both LESS and GREATER, thus a NEVER constraint might be
	 * returned whos isPossible() method returns false.
	 * @return
	 */
	public boolean isPossible();
	
	/**
	 * Return true if the constraint allows the given values {@code valueA} and {@code valueB}.
	 * @param valueA the value from which this constraint is checked
	 * @param valueB the value to which this constraint is checked
	 * @return true if {@code valueA} &lt;constraint&gt; {@code valueB}. (E.G. A < B)
	 */
	public boolean check(T valueA, T valueB);
	
	/**
	 * <p>Should return true when this constraint and {@code other} represent the same thing.
	 * This is necessary to determine when constraint propagation is complete and must be 
	 * implemented correctly.</p>
	 * For example:<br />
	 * {@code if constraintA.equals(constraintB) == true}<br /> 
	 * {@code then constraintA.and(constraintB).equals(constraintA) == true}<br />
	 * {@code and constraintA.and(constraintB).equals(constraintB) == true}<br />
	 */
	public boolean equals(IConstraint<T> other);

	public String toString();
	
	public String symbol();
	
}
