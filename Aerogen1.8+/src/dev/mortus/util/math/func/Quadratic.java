package dev.mortus.util.math.func;

import java.util.ArrayList;
import java.util.List;

import dev.mortus.util.math.geom.Vec2;

public final class Quadratic extends Polynomial {

	public static Function fromPointAndLine(Vec2 point, double lineY) {
		double den = (point.y - lineY)*2;
		if (den == 0) {
			return new Vertical(point.x);
		}
		
		double a = 1 / den;
		double b = -(2*point.x) / den;
		double c = (point.x*point.x + point.y*point.y - lineY*lineY) / den;
		return new Quadratic(a, b, c);
	}
	
	public Quadratic(double a, double b, double c) {
		super(1.0, 0, c, b, a);
	}
	
	/**
	 * Returns a list of the one or two X coordinates of the zeroes of this parabola.
	 * If the equation is constant and equal to zero (I.E. y = 0), there are an unlimited
	 * number of zeros. To represent this case, null is returned. If two zeros exist,
	 * they are added to the list with the lower X coordinate first.
	 * @return list of x coordinates resulting in a zero output, or null if there 
	 * are an unlimited number
	 */
	@Override
	public List<Double> zeros() {
		double a = getCoef(2);
		double b = getCoef(1);
		double c = getCoef(0);
		double range = b*b - 4*a*c;
		
		// No real zeros
		if (range < 0) return new ArrayList<Double>(0);
		
		// Normal quadratic
		if (range == 0) {
			// Parabola touches zero exactly once
			List<Double> zeros = new ArrayList<>(1);
			zeros.add(-b / (2 * a));
			return zeros;
		} else {
			range = Math.sqrt(range);
			
			List<Double> zeros = new ArrayList<>(1);
			zeros.add((-b - range) / (2 * a));
			zeros.add((-b + range) / (2 * a));
			return zeros;
		}
	}
	
	@Override
	public double getValue(double x) {
		double a = getCoef(2);
		double b = getCoef(1);
		double c = getCoef(0);
		return a*x*x + b*x + c;
	}
	
	public static Vec2 getIntersect(Function leftGreater, Function rightGreater) {
		Function difference = rightGreater.subtract(leftGreater);
		
		List<Double> zeros = difference.zeros();
		if (zeros.size() == 0) return null;
		
		Function derivative = difference.derivative();
		if (derivative instanceof Undefined) {
			if (rightGreater instanceof Undefined) {
				return leftGreater.getPoint(zeros.get(0));
			} else {
				return rightGreater.getPoint(zeros.get(0));
			}
		}
		
		// Assuming the functions given are quadratic, we only 
		// care about a single intersect with a positive derivative
		for (double zero : zeros) {
			if (derivative.getValue(zero) > 0) {
				return rightGreater.getPoint(zero);
			}
		}
		
		return null;
	}

	public String toString() {
		double a = getCoef(2);
		double b = getCoef(1);
		double c = getCoef(0);
		return "Quadratic[equation="+a+"x^2 + "+b+"x + "+c+"]";
	}
}
