package dev.mortus.util.math.geom;

public final class LineSeg extends Line {

	private final double length;
	public final Vec2 end;
	
	public LineSeg(Vec2 a, Vec2 b) {
		super(a, b.subtract(a));
		length = b.subtract(a).length();
		end = b;
	}
	
	protected double tmin() {
		return 0;
	}
	
	protected double tmax() {
		return length;
	}

	public double length() {
		return length;
	}
	
	public Line toLine() {
		return new Line(pos, dir);
	}
	
}