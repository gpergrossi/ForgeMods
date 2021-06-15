package com.gpergrossi.constraints.matrix;

import com.gpergrossi.constraints.generic.IConstraint;

public abstract class MatrixEntry<ConstraintClass extends IConstraint<ConstraintClass, ?>> {
	
	protected final ConstraintMatrix<ConstraintClass> matrix;
	
	protected MatrixEntry(ConstraintMatrix<ConstraintClass> matrix) {
		if (matrix == null) throw new IllegalArgumentException("matrix must be non-null!");
		this.matrix = matrix;
	}
	
	protected abstract void setConstraint(ConstraintClass constraint);
	protected abstract void setNeedsUpdate(boolean needsUpdate);
	protected abstract void backup();
	protected abstract void restore();
	
	
	public final ConstraintMatrix<ConstraintClass> getMatrix() {
		return matrix;
	}
	
	public abstract int getIndexA();
	public abstract int getIndexB();

	public abstract ConstraintClass getConstraint();
		
	public final boolean andConstraint(ConstraintClass constraint) {
		return matrix.andConstraint(this.getIndexA(), constraint, this.getIndexB());
	}
	
	public abstract MatrixEntry<ConstraintClass> reverse();
	
	@Override
	public final String toString() {
		return "[v"+getIndexA()+getConstraint().inline()+"v"+getIndexB()+"]";
	}

	
	
	
	public static class Prototype<ConstraintClass extends IConstraint<ConstraintClass, ?>> extends MatrixEntry<ConstraintClass> {
		protected final ConstraintMatrix<ConstraintClass> matrix;
		protected final int indexA, indexB;
		protected ConstraintClass constraint;

		public Prototype(ConstraintMatrix<ConstraintClass> matrix, int indexA, int indexB, ConstraintClass constraint) {
			super(matrix);
			if (indexA >= matrix.size()) throw new IllegalArgumentException("indexA is out of bounds for matrix!");
			if (indexB >= matrix.size()) throw new IllegalArgumentException("indexB is out of bounds for matrix!");
			if (constraint == null) throw new IllegalArgumentException("Constraint must be non-null!");
			this.matrix = matrix;
			this.indexA = indexA;
			this.indexB = indexB;
			this.constraint = constraint;
		}

		@Override
		protected void setConstraint(ConstraintClass constraint) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void setNeedsUpdate(boolean needsUpdate) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		protected void backup() {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void restore() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public int getIndexA() {
			return indexA;
		}

		@Override
		public int getIndexB() {
			return indexB;
		}
		
		@Override
		public ConstraintClass getConstraint() {
			return constraint;
		}
		
		@Override
		public MatrixEntry<ConstraintClass> reverse() {
			throw new UnsupportedOperationException();
		}
	}
	
	public static class Stored<ConstraintClass extends IConstraint<ConstraintClass, ?>> extends MatrixEntry<ConstraintClass> {
		protected final int indexA, indexB;
		protected ConstraintClass constraint;

		private ConstraintClass reverseConstraint;
		private boolean needsUpdate;	// Recently changed but not considered for doUpdate() yet
		private boolean hasChanged;		// Has changed at all since last backup
		private ConstraintClass backupConstraint;
		private ConstraintClass backupReverseConstrain;

		private final ReverseView<ConstraintClass> reverseEntry;

		protected Stored(ConstraintMatrix<ConstraintClass> matrix, int indexA, int indexB, ConstraintClass constraint) {
			super(matrix);
			if (indexA >= matrix.size()) throw new IllegalArgumentException("indexA is out of bounds for matrix!");
			if (indexB >= matrix.size()) throw new IllegalArgumentException("indexB is out of bounds for matrix!");
			if (indexA <= indexB) throw new IllegalArgumentException("indexA must be greater than indexB!");
			if (constraint == null) throw new IllegalArgumentException("Constraint must be non-null!");
			this.indexA = indexA;
			this.indexB = indexB;
			this.constraint = constraint;
			this.reverseEntry = new ReverseView<>(this);
		}

		@Override
		protected void setConstraint(ConstraintClass constraint) {
			if (!this.getConstraint().equals(constraint)) {
				this.constraint = constraint;
				this.reverseConstraint = null;
				this.hasChanged = true;
				setNeedsUpdate(true);
			}
		}
		
		protected void setReverseConstraint(ConstraintClass reverse) {
			if (!this.getReverseConstraint().equals(reverse)) {
				this.constraint = null;
				this.reverseConstraint = reverse;
				this.hasChanged = true;
				setNeedsUpdate(true);
			}
		}

		@Override
		protected void setNeedsUpdate(boolean needsUpdate) {
			if (needsUpdate && !this.needsUpdate) this.matrix.implications.mark(this);
			this.needsUpdate = needsUpdate;
		}

		protected ConstraintClass getReverseConstraint() {
			if (reverseConstraint == null)
				reverseConstraint = constraint.reverse();
			return reverseConstraint;
		}

		@Override
		protected void backup() {
			this.backupConstraint = constraint;
			this.backupReverseConstrain = reverseConstraint;
			this.hasChanged = false;
		}

		@Override
		protected void restore() {
			if (hasChanged) {
				this.constraint = backupConstraint;
				this.reverseConstraint = backupReverseConstrain;
				this.hasChanged = false;
				this.needsUpdate = false;
			}
		}

		@Override
		public int getIndexA() {
			return indexA;
		}

		@Override
		public int getIndexB() {
			return indexB;
		}
		
		@Override
		public ConstraintClass getConstraint() {
			if (constraint == null)
				constraint = reverseConstraint.reverse();
			return constraint;
		}
		
		@Override
		public ReverseView<ConstraintClass> reverse() {
			return reverseEntry;
		}
	}
	
	public static class ReverseView<ConstraintClass extends IConstraint<ConstraintClass, ?>> extends MatrixEntry<ConstraintClass> {
		private final Stored<ConstraintClass> storedEntry;
		
		protected ReverseView(Stored<ConstraintClass> entry) {
			super(entry.matrix);
			this.storedEntry = entry;
		}
		
		@Override
		protected void setConstraint(ConstraintClass constraint) {
			storedEntry.setReverseConstraint(constraint);		
		}
		
		@Override
		protected void setNeedsUpdate(boolean needsUpdate) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void backup() {
			storedEntry.backup();
		}

		@Override
		protected void restore() {
			storedEntry.restore();		
		}
		
		@Override
		public int getIndexA() {
			return storedEntry.getIndexB();
		}

		@Override
		public int getIndexB() {
			return storedEntry.getIndexA();
		}

		@Override
		public ConstraintClass getConstraint() {
			return storedEntry.getReverseConstraint();
		}
		
		@Override
		public Stored<ConstraintClass> reverse() {
			return storedEntry;
		}
	}
	

	public static class Identity<ConstraintClass extends IConstraint<ConstraintClass, ?>> extends MatrixEntry<ConstraintClass> {
		private final int index;
		
		public Identity(ConstraintMatrix<ConstraintClass> matrix, int index) {
			super(matrix);
			this.index = index;
		}
		
		@Override
		protected void setConstraint(ConstraintClass constraint) {
			// Do nothing
		}
		
		@Override
		protected void setNeedsUpdate(boolean needsUpdate) {
			// Do nothing
		}

		@Override
		protected void backup() {
			// Do nothing
		}

		@Override
		protected void restore() {
			// Do nothing
		}
		
		@Override
		public int getIndexA() {
			return index;
		}

		@Override
		public int getIndexB() {
			return index;
		}

		@Override
		public ConstraintClass getConstraint() {
			return matrix.constraintClass.getEqualConstraint();
		}
		
		@Override
		public Identity<ConstraintClass> reverse() {
			return this;
		}
	}

}
