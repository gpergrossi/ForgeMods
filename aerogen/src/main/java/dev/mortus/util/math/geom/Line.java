package dev.mortus.util.math.geom;

import dev.mortus.util.data.Pair;

public class Line {

	protected double x, y;
	protected double dx, dy;
	
	protected Line() {}
	
	public Line(double x, double y, double dx, double dy) {
		this.x = x;
		this.y = y;
		double length = Vec2.distance(0, 0, dx, dy);
		this.dx = dx / length;
		this.dy = dy / length;
	}
	
	public Line copy() {
		return new Line(x, y, dx, dy);
	}
	
	public LineSeg toSegment(double tEnd) {
		return new LineSeg(x - dx * tEnd, y - dy * tEnd, x + dx * tEnd, y + dy * tEnd);
	}

	public LineSeg toSegment(double tStart, double tEnd) {
		return new LineSeg(getX(tStart), getY(tStart), getX(tEnd), getY(tEnd));
	}
	
	public void get(Vec2 ptr, double t) {
		if (ptr == null) return;
		ptr.x = getX(t);
		ptr.y = getY(t);
	}
	
	public double getX(double t) {
		return x+dx*t;
	}
	
	public double getY(double t) {
		return y+dy*t;
	}

	public void getStart(Vec2 ptr) {
		ptr.x = getStartX();
		ptr.y = getStartY();
	}
	
	public double getStartX() {
		return getX(tmin());
	}
	
	public double getStartY() {
		return getY(tmin());
	}

	public void getEnd(Vec2 ptr) {
		ptr.x = getEndX();
		ptr.y = getEndY();
	}
	
	public double getEndX() {
		return getX(tmax());
	}
	
	public double getEndY() {
		return getY(tmax());
	}
	
	protected Line redefine(double tmin, double tmax) {
		if (tmin == this.tmin() && tmax == this.tmax()) return this;
		
		// Infinite line
		if (tmin == Double.NEGATIVE_INFINITY && tmax == Double.POSITIVE_INFINITY) return new Line(x, y, dx, dy);
		
		// Ray
		if (tmax == Double.POSITIVE_INFINITY) return new Ray(getX(tmin), getY(tmin), dx, dy);
		if (tmin == Double.NEGATIVE_INFINITY) return new Ray(getX(tmax), getY(tmax), dx, dy);
		
		// Segment
		return new LineSeg(getX(tmin), getY(tmin), getX(tmax), getY(tmax));
	}

	public boolean intersect(Vec2 ptr, Line other) {
		Pair<Double> tValues = getIntersectTValues(this, other, true);
		if (tValues == null) return false;
		get(ptr, tValues.first);
		return true;
	}
	
	private static Pair<Double> getIntersectTValues(Line first, Line second, boolean canFail) {
		double deltaX = second.x - first.x;
		double deltaY = second.y - first.y;
		
		double det = Vec2.cross(second.dx, second.dy, first.dx, first.dy);
		if (Math.abs(det) < Vec2.EPSILON) return null; // the rays are parallel or one ray has a 0 length direction vector

		double u = Vec2.cross(second.dx, second.dy, deltaX, deltaY) / det;
		double v = Vec2.cross(first.dx,  first.dy,  deltaX, deltaY) / det;
		
		// No collision if t values outside of [tmin(), tmax()]. However we use EPISLON to resolve rounding issues
		if (canFail) {
			if (u+Vec2.EPSILON < first.tmin()  || u-Vec2.EPSILON > first.tmax() ) return null;
			if (v+Vec2.EPSILON < second.tmin() || v-Vec2.EPSILON > second.tmax()) return null;
		}
	
		// Given that u and v can be EPSILON away from the tmin() and tmax() values
		// We must correct them, in order to prevent rounding errors elsewhere
		u = Math.max(u, first.tmin() );
		u = Math.min(u, first.tmax() );
		v = Math.max(v, second.tmin());
		v = Math.min(v, second.tmax());
		
		return new Pair<Double>(u, v); // u is the t value for the first line, v is for second
	}
	
	public double closestPoint(Vec2 in, Vec2 out) {
		Line line = new Line(in.x, in.y, this.dy, -this.dx);
		Pair<Double> tvals = getIntersectTValues(this, line, false);
		if (tvals == null) {
			System.err.println("no closest point: "+this+" AND "+in);
			return Double.POSITIVE_INFINITY;
		} else {
			out.x = this.getX(tvals.first);
			out.y = this.getY(tvals.first);
			return in.distanceTo(out);
		}
	}
	

	public double tmin() {
		return Double.NEGATIVE_INFINITY;
	}
	
	public double tmax() {
		return Double.POSITIVE_INFINITY;
	}
	
	public double length() {
		if (dx == 0 && dy == 0) return 0;
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
		Pair<Double> intersect = getIntersectTValues(this, line, true);
		
		if (intersect == null) {
			double deltaX = line.x - this.x;
			double deltaY = line.y - this.y;
			if (Vec2.cross(dx, dy, deltaX, deltaY) > 0) {
				return new Pair<Line>(line, null); 
			} else {
				return new Pair<Line>(null, line);
			}
		}
		
		double t = intersect.second;
		
		Line lower = null, upper = null;
		
		if (t >= line.tmin()) lower = line.redefine(line.tmin(), t);
		if (t <= line.tmax()) upper = line.redefine(t, line.tmax());
		
		if (Vec2.cross(this.dx, this.dy, line.dx, line.dy) > 0) {
			return new Pair<Line>(upper, lower); 
		} else {
			return new Pair<Line>(lower, upper);
		}
	}
	
	@Override
	public String toString() {
		return "Line[x="+x+", y="+y+", dx="+dx+", dy="+dy+", tmin="+tmin()+", tmax="+tmax()+"]";
	}
	
}
