package dev.mortus.util.math;

import dev.mortus.util.data.Pair;

public class Line {

	public final Vec2 pos;
	public final Vec2 dir;
	
	public Line(Vec2 pos, Vec2 dir) {
		this.pos = pos;
		this.dir = dir.normalize();
	}
	
	public Vec2 getPoint(double t) {
		return pos.add(dir.multiply(t));
	}
	
	protected Line redefine(double tmin, double tmax) {
		if (tmin == this.tmin() && tmax == this.tmax()) return this;
		
		// Infinite line
		if (tmin == Double.NEGATIVE_INFINITY && tmax == Double.POSITIVE_INFINITY) return new Line(pos, dir);
		
		// Ray
		if (tmax == Double.POSITIVE_INFINITY) return new Ray(getPoint(tmin), dir);
		if (tmin == Double.NEGATIVE_INFINITY) return new Ray(getPoint(tmax), dir, true);
		
		// Segment
		return new LineSeg(getPoint(tmin), getPoint(tmax));
	}

	public Vec2 intersect(Line other) {
		Pair<Double> tValues = getIntersectTValues(this, other);
		if (tValues == null) return null;		
		return getPoint(tValues.first);
	}
	
	private static Pair<Double> getIntersectTValues(Line first, Line second) {
		Vec2 delta = second.pos.subtract(first.pos);
		
		double det = second.dir.cross(first.dir);
		if (det == 0) return null; // the rays are parallel or one ray has a 0 dir

		double u = second.dir.cross(delta) / det;
		double v = first.dir.cross(delta) / det;
		
		if (u < first.tmin() || u > first.tmax()) return null;
		if (v < second.tmin() || v > second.tmax()) return null;
	
		return new Pair<Double>(u, v); // u is t for first, v is t for second
	}
	

	protected double tmin() {
		return Double.NEGATIVE_INFINITY;
	}
	
	protected double tmax() {
		return Double.POSITIVE_INFINITY;
	}
	
	public double length() {
		if (dir.length() == 0) return 0;
		return Double.POSITIVE_INFINITY;
	}

	/**
	 * <p>This line slices the given line into two parts and returns the results in a Pair.</p>
	 * <p>All lines are defined by an origin point, a direction, and a range of t values. 
	 * In this system the equation of a line is (origin + t*direction) from tmin to tmax.</p>
	 * <p>The direction determines how slicing is carried out. In the Pair returned, the first 
	 * element is the partial line (Ray/LineSeg) to the LEFT of this line (relative to the direction), 
	 * and the second element is to the RIGHT.</p> 
	 * <p>If this line does not intersect the provided line, one side of the Pair will be null. 
	 * Otherwise, both the left and right partial lines will include their intersection point 
	 * with this line. </p>
	 * <p>The direction of each partial line result will remain the same as the
	 * original, unsliced line. Rays can be oriented (left vs. right) differently than their
	 * direction of extension.</p>
	 * @param line - line to be clipped
	 * @return Pair of partial lines, first = left side, second = right side
	 */
	public Pair<Line> slice(Line line) {
		if (line == null) return new Pair<Line>(null, null);
		Pair<Double> intersect = getIntersectTValues(this, line);
		
		if (intersect == null) {
			Vec2 diff = line.pos.subtract(this.pos);
			if (this.dir.cross(diff) > 0) return new Pair<Line>(line, null); 
			else return new Pair<Line>(null, line);
		}
		
		double t = intersect.second;
		
		Line lower = null, upper = null;
		
		if (t >= line.tmin()) lower = line.redefine(line.tmin(), t);
		if (t <= line.tmax()) upper = line.redefine(t, line.tmax());
		
		if (this.dir.cross(line.dir) > 0) return new Pair<Line>(upper, lower); 
		else return new Pair<Line>(lower, upper);
	}
	
	@Override
	public String toString() {
		return "Line[pos="+pos+", dir="+dir+", tmin="+tmin()+", tmax="+tmax()+"]";
	}
	
}
