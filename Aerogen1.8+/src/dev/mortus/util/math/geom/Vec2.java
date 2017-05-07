package dev.mortus.util.math.geom;

import java.awt.geom.Point2D;

public interface Vec2 {

	public static final double EPSILON = 0.0001;
	public static final double EPSILON2 = EPSILON*EPSILON;
	
	public static final Vec2 ZERO = Vec2.create(0, 0);
	
	public double getX();
	public double getY();

	public int compareTo(Vec2 vec);
	public boolean equals(Vec2 vec); 
	
	public Vec2 multiply(double s);
	public Vec2 divide(double s);
	public Vec2 add(Vec2 other);
	public Vec2 subtract(Vec2 other);
	
	public double cross(Vec2 other);
	public double dot(Vec2 other);
	
	public double angle();
	public double length();
	
	public Vec2 normalize();
	
	public Point2D toPoint2D();
	
	
	public static Vec2 create(double x, double y) {
		return new Direct(x, y);
	}

	public static Vec2 create(Point2D point) {
		return new Direct(point);
	}
	
	public static final class Direct implements Vec2 {

		public static long ALLOCATION_COUNT;
		
		public final double x, y;
		
		public Direct(double x, double y) {
			ALLOCATION_COUNT++;
			this.x = x;
			this.y = y;
		}
		
		public Direct(Point2D point) {
			this.x = point.getX();
			this.y = point.getY();
		}
		
		public Point2D toPoint2D() {
			return new Point2D.Double(x, y);
		}
		
		public Vec2 multiply(double s) {
			return new Direct(x*s, y*s);
		}
		
		public Vec2 divide(double s) {
			return new Direct(x/s, y/s);
		}
		
		public Vec2 add(Vec2 other) {
			return new Direct(x+other.getX(), y+other.getY());
		}
		
		public Vec2 subtract(Vec2 other) {
			return new Direct(x-other.getX(), y-other.getY());
		}
		
		public double cross(Vec2 other) {
			return this.x*other.getY() - this.y*other.getX();
		}
		
		public double dot(Vec2 other) {
			return this.x*other.getX() + this.y*other.getY();
		}

		public double angle() {
			return Math.atan2(y, x);
		}
		
		public double length() {
			return Math.sqrt(x*x + y*y);
		}
		
		public Vec2 normalize() {
			if (length() == 1.0) return this;
			return this.divide(length());
		}
		
		@Override
		public String toString() {
			return "Vec2[x="+x+", y="+y+"]";
		}

		@Override
		public int compareTo(Vec2 other) {
			if (this.y < other.getY()) return -1;
			if (this.y > other.getY()) return 1;
			
			if(this.x < other.getX()) return -1;
			if(this.x > other.getX()) return 1;
			
			return Integer.compare(this.hashCode(), other.hashCode());
		}

		@Override
		public boolean equals(Vec2 other) {
			double dx = this.x - other.getX();
			double dy = this.y - other.getY();
			if (dx*dx + dy*dy < EPSILON2) return true;
			return false;
		}

		@Override
		public double getX() {
			return x;
		}

		@Override
		public double getY() {
			return y;
		}
		
	}

}
