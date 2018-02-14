package com.gpergrossi.constraints.matrix;

import java.util.function.Consumer;

import com.gpergrossi.constraints.generic.IConstraint;
import com.gpergrossi.constraints.generic.IConstraintClass;
import com.gpergrossi.constraints.matrix.MatrixEntry.Stored;

public class ConstraintMatrix<ConstraintClass extends IConstraint<ConstraintClass, ?>> {
	
	public final int size;
	public final IConstraintClass<ConstraintClass> constraintClass;

	protected final int numIndices;
	protected final MatrixEntry.Stored<?>[] matrix;
	protected final ImplicationRules<ConstraintClass> implications;
	
	protected boolean updatesInProgress;
	
	public ConstraintMatrix(IConstraintClass<ConstraintClass> constraintClass, int size) {
		this.size = size;
		this.numIndices = size * (size-1) / 2;
		this.matrix = new MatrixEntry.Stored<?>[numIndices];
		this.constraintClass = constraintClass;
		this.implications = constraintClass.getImplicationRules(this);
		init();
	}

	private ConstraintMatrix(ConstraintMatrix<ConstraintClass> matrix) {
		this.size = matrix.size;
		this.numIndices = matrix.numIndices;
		this.matrix = new MatrixEntry.Stored<?>[numIndices];
		this.constraintClass = matrix.constraintClass;
		this.implications = constraintClass.getImplicationRules(this);
		
		// Copy constraints
		for (int i = 0; i < numIndices; i++) {
			@SuppressWarnings("unchecked")
			MatrixEntry.Stored<ConstraintClass> entry = (Stored<ConstraintClass>) matrix.matrix[i];
			int a = entry.indexA;
			int b = entry.indexB;
			ConstraintClass constraint = entry.getConstraint();
			this.matrix[i] = new MatrixEntry.Stored<ConstraintClass>(this, a, b, constraint);
		}
	}
	
	public ConstraintMatrix<ConstraintClass> copy() {
		return new ConstraintMatrix<>(this);
	}
	
	/**
	 * Fill the constraint table with an identity matrix. All variables equal themselves,
	 * an no variables have any relationship with other variables yet. Since the constraint
	 * array only stores the lower half of the matrix, all constraints are set to ALWAYS.
	 */
	protected void init() {
		final ConstraintClass always = constraintClass.getAlwaysConstraint();
		int index = 0;
		for (int i = 1; i < size; i++) {
			for (int j = 0; j < i; j++) {
				matrix[index++] = new MatrixEntry.Stored<ConstraintClass>(this, i, j, always);
			}
		}
	}

	public boolean andConstraint(int indexA, ConstraintClass constraint, int indexB) {
		if (!updatesInProgress) this.backup();
		
		MatrixEntry<ConstraintClass> entryAB = getMatrixEntry(indexA, indexB);
		
		ConstraintClass now = entryAB.getConstraint();
		ConstraintClass combined = now.and(constraint);
		
		if (combined.isPossible()) {
			entryAB.setConstraint(combined);
			if (!updatesInProgress) {
				updatesInProgress = true;
				final boolean success = this.implications.doImplicationUpdates();
				updatesInProgress = false;
				if (!success) this.restore();
				return success;
			}
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public MatrixEntry<ConstraintClass> getMatrixEntry(int indexA, int indexB) {
		if (indexA == indexB) return new MatrixEntry.Identity<>(this, indexA);
		if (indexA < indexB) return getMatrixEntry(indexB, indexA).reverse();
		return (MatrixEntry<ConstraintClass>) matrix[(indexA * (indexA-1) / 2) + indexB];
	}

	public int size() {
		return size;
	}
	
	@SuppressWarnings("unchecked")
	public void iterate(Consumer<MatrixEntry<ConstraintClass>> consumer) {
		for (int i = 0; i < numIndices; i++) {
			consumer.accept((MatrixEntry<ConstraintClass>) matrix[i]);
		}
	}
	
	public void backup() {
		iterate(MatrixEntry::backup);
	}
	
	public void restore() {
		this.implications.unmarkAll();
		iterate(MatrixEntry::restore);
	}
	
	private String extend(String str, int length) {
		String out = "";
		while (out.length() < length) {
			out += str;
		}
		return out.substring(0, length);
	}
	
	public void print() {
		int[] cellWidth = new int[size];
		int minCellWidth = (int) Math.ceil(Math.log10(size)) + 2; // +2 for "v" and " " in "v## |"
		int maxCellWidth = minCellWidth;
		for (int j = 0; j < size; j++) {
			cellWidth[j] = (int) Math.ceil(Math.log10(size));
			for (int i = 0; i < size; i++) {
				cellWidth[j] = Math.max(cellWidth[j], getMatrixEntry(i, j).getConstraint().inline().length());
			}
			maxCellWidth = Math.max(maxCellWidth, cellWidth[j]);
		}
		
		String emptyCellStr = extend(" ", maxCellWidth);
		String dashed = extend("-", maxCellWidth);
		
		// Label Row
		System.out.print(emptyCellStr.substring(0, minCellWidth)+"|");
		for (int j = 0; j < size; j++) {
			String label = emptyCellStr+"v"+j+" ";
			label = label.substring(label.length() - cellWidth[j]);
			System.out.print(label+"|");
		}
		System.out.println();
		
		// Label Row Divider ------+------+----- 
		System.out.print(dashed.substring(0, minCellWidth)+"+");
		for (int j = 0; j < size; j++) {
			String label = dashed.substring(dashed.length() - cellWidth[j]);
			System.out.print(label+"+");
		}
		System.out.println();
		
		for (int i = 0; i < size; i++) {
			String label = emptyCellStr+"v"+i+" ";
			label = label.substring(label.length() - minCellWidth);
			System.out.print(label+"|");
			
			for (int j = 0; j < size; j++) {
				String value = emptyCellStr+getMatrixEntry(i, j).getConstraint().inline();
				value = value.substring(value.length() - cellWidth[j]);
				System.out.print(value+"|");
			}
			
			System.out.println();
		}
		System.out.println();
	}
	
}
