package dev.mortus.util.math.geom;

import java.awt.geom.Point2D;

public final class Vec2 implements Comparable<Vec2> {

	public static final double EPSILON = 0.000001;

	public static final Vec2 ZERO = new Vec2(0, 0);
	
	public final double x, y;
	
	private double length = Double.MAX_VALUE;
	private double angle = Double.MAX_VALUE;
	
	public Vec2(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Vec2(Point2D position) {
		this.x = position.getX();
		this.y = position.getY();
	}
	
	public Point2D toPoint2D() {
		return new Point2D.Double(x, y);
	}
	
	public Vec2 multiply(double s) {
		return new Vec2(x*s, y*s);
	}
	
	public Vec2 divide(double s) {
		return new Vec2(x/s, y/s);
	}
	
	public Vec2 add(Vec2 other) {
		return new Vec2(x+other.x, y+other.y);
	}
	
	public Vec2 subtract(Vec2 other) {
		return new Vec2(x-other.x, y-other.y);
	}
	
	public double cross(Vec2 other) {
		return this.x*other.y - this.y*other.x;
	}

	public double angle() {
		if (angle == Double.MAX_VALUE) {
			angle = Math.atan2(y, x);
		}
		return angle;
	}
	
	public double length() {
		if (length == Double.MAX_VALUE) {
			length = Math.sqrt(x*x + y*y);
		}
		return length;
	}
	
	public Vec2 normalize() {
		if (length() == 1.0) return this;
		return this.divide(length());
	}
	
	public boolean equals(Vec2 other) {
		if (this.subtract(other).length() < EPSILON) return true;
		return false;
//		return this.x == other.x && this.y == other.y;
	}
	
	@Override
	public String toString() {
		return "Vec2[x="+x+", y="+y+"]";
	}

	@Override
	public int compareTo(Vec2 other) {
		if (this.y < other.y) return -1;
		if (this.y > other.y) return 1;
		
		if(this.x < other.x) return -1;
		if(this.x > other.x) return 1;
		
		return Integer.compare(this.hashCode(), other.hashCode());
	}
	
}
