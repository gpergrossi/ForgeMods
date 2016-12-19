package dev.mortus.util.math.geom;

public final class Ray extends Line {

	final boolean reverse;
	
	public Ray(Vec2 pos, Vec2 dir) {
		super(pos, dir);
		this.reverse = false;
	}
	
	public Ray(Vec2 pos, Vec2 dir, boolean reverse) {
		super(pos, dir);
		this.reverse = reverse;
	}
	
	public Ray extend(double d) {
		if (reverse) return new Ray(pos.add(dir.multiply(d)), dir, true);
		return new Ray(pos.add(dir.multiply(-d)), dir);
	}
	
	protected double tmin() {
		if (reverse) return Double.NEGATIVE_INFINITY;
		return 0;
	}
	
	protected double tmax() {
		if (reverse) return 0;
		return Double.POSITIVE_INFINITY;
	}
	
}
