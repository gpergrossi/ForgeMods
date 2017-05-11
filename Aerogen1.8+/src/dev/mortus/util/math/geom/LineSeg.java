package dev.mortus.util.math.geom;

public class LineSeg extends Line {
	
	public LineSeg(double x0, double y0, double x1, double y1) {
		this.x = x0;
		this.y = y0;
		this.dx = (x1-x0);
		this.dy = (y1-y0);
	}

	public LineSeg copy() {
		return new LineSeg(x, y, dx, dy);
	}
	
	public LineSeg createLineSegment(double maxExtent) {
		return this.copy();
	}
	
	public double tmin() {
		return 0;
	}
	
	public double tmax() {
		return 1;
	}

	public double length() {
		return Vec2.distance(0, 0, dx, dy);
	}
	
	public Line toLine() {
		return new Line(x, y, dx, dy);
	}
	
}
