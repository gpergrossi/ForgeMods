package dev.mortus.util.math.func2d;

public class RemapOperation implements Function2D {

	public static interface Operation {
		public double remap(double a);
	}
	
	Function2D noiseA;
	Operation operation;
	
	public RemapOperation(Function2D a, Operation op) {
		this.noiseA = a;
		this.operation = op;
	}
	
	@Override
	public double getValue(double x, double y) {
		double a = 0;
		if (noiseA != null) a = noiseA.getValue(x, y);
		return operation.remap(a);
	}
	
}
