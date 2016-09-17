package dev.mortus.util.math;

public class Ray {

	public final Vec2 pos;
	public final Vec2 dir;

	public Ray(Vec2 pos, Vec2 dir) {
		if (pos == null) throw new RuntimeException("Null position");
		if (dir == null) throw new RuntimeException("Null direction");
		this.pos = pos;
		this.dir = dir.normalize();
	}
	
	public Vec2 intersect(Ray other) {
		double dx = other.pos.x - this.pos.x;
		double dy = other.pos.y - this.pos.y;
		double det = other.dir.x * this.dir.y - other.dir.y * this.dir.x;
		
		if (det == 0) return null; // the rays are parallel or one ray has a 0 dir
		
		double u = (dy * other.dir.x - dx * other.dir.y) / det;
		double v = (dy * this.dir.x - dx * this.dir.y) / det;

		if (u < 0 || v < 0) return null; // intersection is behind one of the rays' position
		
		return pos.add(dir.multiply(u));
	}
	
	public Ray lengthen(double d) {
		return new Ray(pos.add(dir.multiply(-d)), dir);
	}
	
}
