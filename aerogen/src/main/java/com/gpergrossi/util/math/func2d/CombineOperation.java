package com.gpergrossi.util.math.func2d;

public class CombineOperation implements IFunction2D {

	public static interface Operation {
		public double combine(double a, double b);
	}
	
	IFunction2D noiseA, noiseB;
	Operation operation;
	
	public CombineOperation(IFunction2D a, IFunction2D b, Operation op) {
		this.noiseA = a;
		this.noiseB = b;
		this.operation = op;
	}
	
	@Override
	public double getValue(double x, double y) {
		double a = 0, b = 0;
		
		if (noiseA != null) a = noiseA.getValue(x, y);
		if (noiseB != null) b = noiseB.getValue(x, y);
		return operation.combine(a, b);
	}

}
