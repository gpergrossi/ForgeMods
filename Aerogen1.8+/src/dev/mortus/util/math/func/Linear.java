package dev.mortus.util.math.func;

import java.util.ArrayList;
import java.util.List;

public class Linear extends Polynomial {
	
	public Linear (double m, double b) {
		super(1.0, 0, b, m);
	}
	
	@Override
	public List<Double> zeros() {
		List<Double> zeros = new ArrayList<>();
		double m = getCoef(1);
		double b = getCoef(0);
		zeros.add(-b/m);
		return zeros;
	}
	
	@Override
	public double getValue(double x) {
		double m = getCoef(1);
		double b = getCoef(0);
		return m*x+b;
	}
	
}
