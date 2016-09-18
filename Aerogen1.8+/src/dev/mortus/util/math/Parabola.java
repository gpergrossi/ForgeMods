package dev.mortus.util.math;

import dev.mortus.util.data.Pair;

public class Parabola {
	public final double a, b, c;
	
	public final boolean isVertical;
	public final double verticalX;

	public static Parabola fromPointAndLine(Vec2 point, double lineY) {
		double den = (point.y - lineY)*2;
		if (den == 0) {
			return new Parabola(point.x);
		}
		
		double a = 1 / den;
		double b = -(2*point.x) / den;
		double c = (point.x*point.x + point.y*point.y - lineY*lineY) / den;
		return new Parabola(a, b, c);
	}
	
	private Parabola(double verticalX) {
		a = b = c = Double.NaN;
		this.isVertical = true;
		this.verticalX = verticalX;
	}
	
	public Parabola(double a, double b, double c) {
		this.a = a;
		this.b = b;
		this.c = c;
		
		this.isVertical = false;
		this.verticalX = Double.NaN;
	}
	
	public double get(double x) {
		return a*x*x + b*x + c;
	}
	
	/**
	 * Will return the intersecting points of this parabola with the given other parabola.
	 * If the parabolas are identical, null is returned to represent an unlimited number 
	 * of solutions.
	 * @param other parabola to intersect with
	 * @return list of intersecting points or null if there are an unlimited number
	 */
	public Pair<Vec2> intersect(Parabola other) {
		Parabola composite = this.subtract(other);
		Pair<Double> zeros = composite.zeros();
		
		if (!this.isVertical) { 
			return new Pair<Vec2>(getPoint(zeros.first), getPoint(zeros.second));
		} else if (!other.isVertical) {
			return new Pair<Vec2>(other.getPoint(zeros.first), other.getPoint(zeros.second));
		} else {
			return new Pair<Vec2>(null, null);
		}
	}
	
	private Vec2 getPoint(Double x) {
		if (x == null) return null;
		return new Vec2(x, get(x));
	}
	
	/**
	 * Returns a list of the one or two X coordinates of the zeroes of this parabola.
	 * If the equation is constant and equal to zero (I.E. y = 0), there are an unlimited
	 * number of zeros. To represent this case, null is returned. If two zeros exist,
	 * they are added to the list with the lower X coordinate first.
	 * @return list of x coordinates resulting in a zero output, or null if there 
	 * are an unlimited number
	 */
	public Pair<Double> zeros() {
		
		// Vertical "parabola"
		if (this.isVertical) {
			if (this.verticalX == Double.NaN) return new Pair<Double>(null, null);
			return new Pair<Double>(this.verticalX, null);
		}
		
		// Non order 2
		if (a == 0) {
			if (b == 0) {
				// constant, returns null
				return null; 
			}
			
			// linear, return single zero
			return new Pair<Double>(-c/b, null);
		}
		
		double range = b*b - 4*a*c;
		
		// No real zeros
		if (range < 0) return new Pair<Double>(null, null);
		
		// Normal quadratic
		if (range == 0) {
			// Parabola touches zero exactly once
			return new Pair<Double>(-b / (2 * a), null);
		} else {
			range = Math.sqrt(range);
			
			// Return zeros so lesser X is first
			if (a > 0) {
				return new Pair<Double>((-b - range) / (2 * a), (-b + range) / (2 * a));
			} else {
				return new Pair<Double>((-b + range) / (2 * a), (-b - range) / (2 * a));
			}
		}
	}
	
	public Parabola subtract(Parabola other) {
		if (this.isVertical) {
			if (other.isVertical) return new Parabola(Double.NaN);
			return this;
		} else if (other.isVertical) {
			return other;
		}
		return new Parabola(a-other.a, b-other.b, c-other.c);
	}
	
	public Parabola add(Parabola other) {
		if (this.isVertical) {
			if (other.isVertical) return new Parabola(Double.NaN);
			return this;
		} else if (other.isVertical) {
			return other;
		}
		return new Parabola(a+other.a, b+other.b, c+other.c);
	}
}
