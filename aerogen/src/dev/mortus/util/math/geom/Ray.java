package dev.mortus.util.math.geom;

public final class Ray extends Line {

	protected boolean reversed;
	
	public Ray(double x, double y, double dx, double dy) {
		super(x, y, dx, dy);
		this.reversed = false;
	}
	
	public Ray(double x, double y, double dx, double dy, boolean reverse) {
		super(x, y, dx, dy);
		this.reversed = reverse;
	}

	public LineSeg createLineSegment(double maxExtent) {
		LineSeg seg = null;
		if (reversed) {
			seg = new LineSeg(dx * -maxExtent, dy * -maxExtent, x, y);
		} else {
			seg = new LineSeg(x, y, dx * maxExtent, dy * maxExtent);
		}
		return seg;
	}
	
	public Ray copy() {
		return new Ray(x, y, dx, dy, reversed);
	}
	
	public void extend(double d) {
		d = (reversed ? -d : d);
		x += dx*d;
		y += dy*d;
	}
	
	public double tmin() {
		return (reversed ? Double.NEGATIVE_INFINITY : 0);
	}
	
	public double tmax() {
		return (reversed ? 0 : Double.POSITIVE_INFINITY);
	}
	
}
