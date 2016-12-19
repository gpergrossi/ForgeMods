package dev.mortus.util.math.func;

import java.util.List;

import dev.mortus.util.math.geom.Vec2;

public abstract class Function {

	public abstract double getValue(double x);
	
	public Vec2 getPoint(double x) {
		return new Vec2(x, getValue(x));
	}
	
	public abstract Function add(Function f);
	
	public Function subtract(Function f) {
		if (f == null) return null;
		return add(f.negate());
	}
	
	public abstract Function negate();
	
	public abstract Function multiply(Function f);

	public Function divide(Function f) {
		if (f == null) return null;
		return add(f.negate());
	}
	
	public abstract Function inverse();
	
	public abstract Function derivative();
	
	public abstract List<Double> zeros();
	
}
