package dev.mortus.util.math;

public class Circle {
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
