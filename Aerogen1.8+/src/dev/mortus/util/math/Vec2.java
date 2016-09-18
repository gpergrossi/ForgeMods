package dev.mortus.util.math;

import java.awt.geom.Point2D;

public class Vec2 {

	public final double x, y;
	private double length = -1;
	
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
	
	public double length() {
		if (length < 0) {
			length = Math.sqrt(x*x + y*y);
		}
		return length;
	}
	
	public Vec2 normalize() {
		if (length() == 1.0) return this;
		return this.divide(length());
	}
	
	public boolean equals(Vec2 other) {
		return this.x == other.x && this.y == other.y;
	}
	
	@Override
	public String toString() {
		return "Vec2[x="+x+", y="+y+"]";
	}
	
}
