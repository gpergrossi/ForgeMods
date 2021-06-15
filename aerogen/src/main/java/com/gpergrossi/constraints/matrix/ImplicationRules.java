package com.gpergrossi.constraints.matrix;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import com.gpergrossi.constraints.generic.IConstraint;

public abstract class ImplicationRules<ConstraintClass extends IConstraint<ConstraintClass, ?>> {

	protected final ConstraintMatrix<ConstraintClass> matrix;
	private final Set<MatrixEntry<ConstraintClass>> updateSet;
	private final Queue<MatrixEntry<ConstraintClass>> updateQueue;
	
	public ImplicationRules(ConstraintMatrix<ConstraintClass> matrix) {
		if (matrix == null) throw new IllegalArgumentException("matrix must be non-null!");
		this.matrix = matrix;
		this.updateSet = new HashSet<>();
		this.updateQueue = new LinkedList<>();
	}
	
	public ConstraintMatrix<ConstraintClass> getMatrix() {
		return this.matrix;
	}
	
	public boolean mark(MatrixEntry<ConstraintClass> entry) {
		if (entry.getMatrix() != this.matrix) throw new IllegalArgumentException("entry matrix must match implication matrix!");
		boolean added = this.updateSet.add(entry);
		if (added) updateQueue.offer(entry);
		return added;
	}
	
	public boolean unmark(MatrixEntry<ConstraintClass> entry) {
		if (entry.getMatrix() != this.matrix) throw new IllegalArgumentException("entry matrix must match implication matrix!");
		entry.setNeedsUpdate(false);
		return this.updateSet.remove(entry);
	}

	public void unmarkAll() {
		for (MatrixEntry<ConstraintClass> entry : updateSet) {
			entry.setNeedsUpdate(false);
		}
		this.updateSet.clear();
		this.updateQueue.clear();
	}
	
	protected MatrixEntry<ConstraintClass> poll() {
		MatrixEntry<ConstraintClass> entry = updateQueue.poll();
		while (entry != null) {
			boolean hadEntry = updateSet.remove(entry);
			if (hadEntry) {
				entry.setNeedsUpdate(false);
				return entry;
			}
			else entry = updateQueue.poll();
		}
		return null;
	}
	
	/**
	 * <p>This method propagate the implication represented by this class until no more
	 * updates are caused.</p>
	 * <p>Use {@link mark} and {@link poll} to continue searching for updateable entries 
	 * in the matrix. {@link unmark} is also provided in case it has a purpose, however 
	 * {@link poll} already unmarks entries that are polled.</p>
	 * @return true if after all implications there were no contradictions, false if a contradiction arose.
	 */
	public abstract boolean doImplicationUpdates();
	
}
