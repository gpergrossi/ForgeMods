package dev.mortus.util.math.func;

import java.util.ArrayList;
import java.util.List;

public class Undefined extends Function {

	public Undefined() {}
	
	@Override
	public double getValue(double x) {
		return Double.NaN;
	}

	@Override
	public Function add(Function f) {
		return this;
	}

	@Override
	public Function negate() {
		return this;
	}

	@Override
	public Function multiply(Function f) {
		return this;
	}

	@Override
	public Function inverse() {
		return this;
	}

	@Override
	public Function derivative() {
		return this;
	}

	@Override
	public List<Double> zeros() {
		return new ArrayList<>(0);
	}
	
	public String toString() {
		return "Undefined";
	}

}
