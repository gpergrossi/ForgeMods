package dev.mortus.voronoi.internal;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MathUtil {
	
	public static class Vec2 {
		public final double x, y;
		public Vec2(double x, double y) {
			this.x = x;
			this.y = y;
		}
		public Vec2(Point2D position) {
			this.x = position.getX();
			this.y = position.getY();
		}
		public Point2D toPoint() {
			return new Point2D.Double(x, y);
		}
	}
	
	public static class Vec3 extends Vec2 {
		public final double z;
		public Vec3(double x, double y, double z) {
			super(x, y);
			this.z = z;
		}
	}
	
	public static class Vec4 extends Vec3 {
		public final double w;
		public Vec4(double x, double y, double z, double w) {
			super(x, y, z);
			this.w = w;
		}
	}
	
	public static class Parabola {
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
		public List<Vec2> intersect(Parabola other) {
			Parabola composite = this.subtract(other);
			List<Double> zeros = composite.zeros();
			if (zeros == null) return null;
			List<Vec2> intersects;
			if (!this.isVertical) { 
				intersects = zeros.stream()
				.map(s -> new Vec2(s, get(s)))
				.collect(Collectors.toList());
			} else if (!other.isVertical) {
				intersects = zeros.stream()
				.map(s -> new Vec2(s, other.get(s)))
				.collect(Collectors.toList());
			} else {
				return null;
			}
			return Collections.unmodifiableList(intersects);
		}
		
		/**
		 * Returns a list of the one or two X coordinates of the zeroes of this parabola.
		 * If the equation is consta/*nt and equal to zero (I.E. y = 0), there are an unlimited
		 * number of zeros. To represent this case, null is returned. If two zeros exist,
		 * they are added to the list with the lower X coordinate first.
		 * @return list of x coordinates resulting in a zero output, or null if there 
		 * are an unlimited number
		 */
		public List<Double> zeros() {
			List<Double> zeros = new ArrayList<Double>();
			
			// Vertical "parabola"
			if (this.isVertical) {
				if (this.verticalX == Double.NaN) return null;
				zeros.add(this.verticalX);
				return Collections.unmodifiableList(zeros);
			}
			
			// Non order 2
			if (a == 0) {
				if (b == 0) {
					// constant, returns null
					return null; 
				}
				
				// linear, return single zero
				zeros.add(-c/b);
				return Collections.unmodifiableList(zeros);
			}
			
			double partial = b*b - 4*a*c;
			
			// No zeros
			if (partial < 0) return Collections.unmodifiableList(zeros);
			
			if (partial == 0) {
				zeros.add(-b / (2 * a));
			} else {
				partial = Math.sqrt(partial);
				
				// Add zeros so lesser X is first
				if (a > 0) {
					zeros.add((-b - partial) / (2 * a));
					zeros.add((-b + partial) / (2 * a));
				} else {
					zeros.add((-b + partial) / (2 * a));
					zeros.add((-b - partial) / (2 * a));
				}
			}
			return Collections.unmodifiableList(zeros);
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
	
	public static class Circle {
		
		public final double x, y, radius;
		
		public static Circle fromPoints(Vec2 a, Vec2 b, Vec2 c) {
			double abx = a.x - b.x;
			double aby = a.y - b.y;
			double bcx = b.x - c.x;
			double bcy = b.y - c.y;
			
			double d = abx*bcy - bcx*aby;
			if (d == 0) return null;
			
			double u = (a.x*a.x - b.x*b.x + a.y*a.y - b.y*b.y) / 2.0;
			double v = (b.x*b.x - c.x*c.x + b.y*b.y - c.y*c.y) / 2.0;
			
			double x = (u*bcy - v*aby) / d;
			double y = (v*abx - u*bcx) / d;
			
			return new Circle(x, y, Math.sqrt((a.x-x)*(a.x-x) + (a.y-y)*(a.y-y)));
		}
		
		public Circle(double x, double y, double r) {
			this.x = x;
			this.y = y;
			this.radius = r;
		}
		
		public double getX() {
			return x;
		}
		
		public double getY() {
			return y;
		}
		
		public double getRadius() {
			return radius;
		}
		
		@Override
		public String toString() {
			return "Circle[X="+x+", Y="+y+", Radius="+radius+"]";
		}
		
	}
	
}
