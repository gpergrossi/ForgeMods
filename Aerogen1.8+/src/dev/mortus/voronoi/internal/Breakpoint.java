package dev.mortus.voronoi.internal;

import java.awt.geom.Point2D;
import java.util.List;

import dev.mortus.voronoi.internal.MathUtil.Parabola;
import dev.mortus.voronoi.internal.MathUtil.Vec2;

public final class Breakpoint {
	
	public Arc arcLeft, arcRight;
	
	public Breakpoint(Arc left, Arc right) {
		if (left.equals(right)) throw new RuntimeException("Cannot construct breakpoint between identical arcs!");
		this.arcLeft = left;
		this.arcRight = right;
	}
	
	private double lastRequest = Double.NaN;
	private Vec2 lastResult = null;
	
	public Point2D getPos(double sweeplineY) {
		if (sweeplineY != lastRequest) {
			lastRequest = sweeplineY;
			lastResult = getPosInternal(sweeplineY);
		}
		
		return lastResult.toPoint();
	}
	
	private Vec2 getPosInternal(double sweeplineY) {
		Parabola pl = arcLeft.getParabola(sweeplineY);
		Parabola pr = arcRight.getParabola(sweeplineY);
		
		List<Vec2> intersects = pl.intersect(pr);
		assert (intersects != null);

		double leftY = arcLeft.site.getY();
		double rightY = arcRight.site.getY();
		
		if (pl.isVertical) {
			assert (intersects.size() == 1);
			return intersects.get(0);
			
		} else if (leftY == rightY) {
			// only one intersect parabolas are side by side
			assert (intersects.size() == 1);
			return intersects.get(0);
			
		} else if (leftY > rightY) {
			// left parabola is steeper than right 
			// two intersects exist
			// use X+ most intersect (index: 1)
			assert (intersects.size() == 2);
			return intersects.get(1);
			
		} else {
			// right parabola is steeper than left 
			// two intersects exist
			// use X- most intersect (index: 0)
			assert (intersects.size() == 2);
			return intersects.get(0);
		}
	}
	
}
