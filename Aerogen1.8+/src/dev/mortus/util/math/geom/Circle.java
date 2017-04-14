package dev.mortus.util.math.geom;

public final class Circle {
	
	public final double x, y, radius;
	
	public static Circle fromPoints(Vec2 a, Vec2 b, Vec2 c) {
		double abx = a.getX() - b.getX();
		double aby = a.getY() - b.getY();
		double bcx = b.getX() - c.getX();
		double bcy = b.getY() - c.getY();
		
		double d = abx*bcy - bcx*aby;
		if (d == 0) return null;
		
		double u = (a.getX()*a.getX() - b.getX()*b.getX() + a.getY()*a.getY() - b.getY()*b.getY()) / 2.0;
		double v = (b.getX()*b.getX() - c.getX()*c.getX() + b.getY()*b.getY() - c.getY()*c.getY()) / 2.0;
		
		double x = (u*bcy - v*aby) / d;
		double y = (v*abx - u*bcx) / d;
		
		double dx = a.getX()-x;
		double dy = a.getY()-y;
		return new Circle(x, y, Math.sqrt(dx*dx + dy*dy));
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
