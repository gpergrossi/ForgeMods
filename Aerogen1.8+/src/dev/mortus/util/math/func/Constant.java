package dev.mortus.util.math.func;

import java.util.ArrayList;
import java.util.List;

public class Constant extends Polynomial {

	public Constant (double c) {
		super(1.0, 0, c);
	}
	
	@Override
	public List<Double> zeros() {
		return new ArrayList<Double>(0);
	}
	
	@Override
	public double getValue(double x) {
		return getCoef(0);
	}
	
}
