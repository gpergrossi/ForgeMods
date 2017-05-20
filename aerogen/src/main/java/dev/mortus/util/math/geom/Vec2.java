package dev.mortus.util.math.geom;

import java.awt.geom.Point2D;

public class Vec2 implements Comparable<Vec2> {

	public static final double EPSILON = 0.001;
	
	public static long ALLOCATION_COUNT;
	
	public static double distance(double x0, double y0, double x1, double y1) {
		double dx = x1-x0;
		double dy = y1-y0;
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	public static double angle(double x0, double y0, double x1, double y1) {
		double dx = x1-x0;
		double dy = y1-y0;
		return Math.atan2(dy, dx);
	}
	
	public static double cross(double x0, double y0, double x1, double y1) {
		return x0*y1 - y0*x1;
	}
	
	public static double dot(double x0, double y0, double x1, double y1) {
		return x0*x1 + y0*y1;
	}
	
	public static boolean equals(double x0, double y0, double x1, double y1) {
		return (distance(x0, y0, x1, y1) < EPSILON);
	}
	
	protected double x, y;
	
	public Vec2(double x, double y) {
		ALLOCATION_COUNT++;
		this.x = x;
		this.y = y;
	}
	
	public Vec2 copy() {
		return new Vec2(this.x, this.y);
	}
	
	public double x() {
		return x;
	}
	
	public double y() {
		return y;
	}
		
	public Point2D toPoint2D() {
		return new Point2D.Double(x, y);
	}
	
	public void multiply(double s) {
		this.x *= s;
		this.y *= s;
	}
	
	public void divide(double s) {
		this.x /= s;
		this.y /= s;
	}
	
	public void add(Vec2 other) {
		this.x += other.x;
		this.y += other.y;
	}
	
	public void subtract(Vec2 other) {
		this.x -= other.x;
		this.y -= other.y;
	}
	
	public double cross(Vec2 other) {
		return this.x*other.y - this.y*other.x;
	}
	
	public double dot(Vec2 other) {
		return this.x*other.x + this.y*other.y;
	}

	public double distanceTo(Vec2 o) {
		return Vec2.distance(this.x, this.y, o.x, o.y);
	}

	public double distanceTo(double x2, double y2) {
		return Vec2.distance(this.x, this.y, x2, y2);
	}
	
	public double angle() {
		return Math.atan2(y, x);
	}
	
	public double length() {
		return Math.sqrt(x*x + y*y);
	}
	
	public void normalize() {
		this.divide(length());
	}
	
	@Override
	public String toString() {
		return "Vec2[x="+x+", y="+y+"]";
	}

	@Override
	public int compareTo(Vec2 other) {
		int dy = (int) Math.signum(this.y - other.y);
		if (dy != 0) return dy;

		int dx = (int) Math.signum(this.x - other.x);
		if (dx != 0) return dx;
		
		return Integer.compare(this.hashCode(), other.hashCode());
	}

	public boolean equals(Vec2 other) {
		return (distance(this.x, this.y, other.x, other.y) < EPSILON);
	}

}
