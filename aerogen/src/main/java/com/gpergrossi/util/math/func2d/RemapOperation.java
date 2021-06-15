package com.gpergrossi.util.math.func2d;

public class RemapOperation implements IFunction2D {

	@FunctionalInterface
	public static interface Operation {
		public double remap(double a);
	}
	
	IFunction2D noiseA;
	Operation operation;
	
	public RemapOperation(IFunction2D a, Operation op) {
		this.noiseA = a;
		this.operation = op;
	}
	
	@Override
	public double getValue(double x, double y) {
		return operation.remap(noiseA.getValue(x, y));
	}
	
}
