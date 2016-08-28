package dev.mortus.voronoi.internal;

import java.awt.geom.Point2D;
import java.util.List;

import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.internal.MathUtil.Parabola;
import dev.mortus.voronoi.internal.MathUtil.Vec2;

public class Breakpoint extends TreeNode {
	
	public Arc arcLeft, arcRight;
	
	public Breakpoint(Arc left, Arc right) {
		if (left.equals(right)) throw new RuntimeException("Cannot construct breakpoint between identical arcs!");
		
		this.arcLeft = left;
		this.arcRight = right;
		
		if (arcLeft.site.getY() == arcRight.site.getY() && arcLeft.site.getX() > arcRight.site.getX()) {
			// The parabolas are exactly side by side, there is only one intersection between
			// them and the X coordinates of the parabola's focii are in the wrong order for
			// the requested breakpoint to exist.
			throw new RuntimeException("There is no such breakpoint!");
		}
	}

	@Override
	public String getType() {
		return "Breakpoint";
	}

	@Override
	public boolean hasChildren() {
		return true;
	}
	
	private double lastRequest = Double.NaN;
	private Vec2 lastResult = null;
	
	public Point2D getPosition(double sweeplineY) {
		if (sweeplineY != lastRequest) {
			lastRequest = sweeplineY;
			lastResult = calculatePosition(sweeplineY);
		}
		if (lastResult == null) return null;
		return lastResult.toPoint();
	}
	
	private Vec2 calculatePosition(double sweeplineY) {
		Parabola leftParabola = arcLeft.getParabola(sweeplineY);
		Parabola rightParabola = arcRight.getParabola(sweeplineY);
		
		List<Vec2> intersects = leftParabola.intersect(rightParabola);
		double leftY = arcLeft.site.getY();
		double rightY = arcRight.site.getY();
			
		// Case either parabola is a vertical line (focus y coord = directrix y coord)
		if (leftParabola.isVertical && rightParabola.isVertical) {
			// Both parabolas are vertical, no valid intersection
			System.err.println("null intersect ("+arcLeft.site.id+" x "+arcRight.site.id+")");
			return null;
		} else if (leftParabola.isVertical) {
			if (intersects.size() != 1) throw new RuntimeException("Seemingly impossible intersection result, vertical parabola intersects "+intersects.size()+" times");
			return intersects.get(0);
		} else if (rightParabola.isVertical) {
			if (intersects.size() != 1) throw new RuntimeException("Seemingly impossible intersection result, vertical parabola intersects "+intersects.size()+" times");
			return intersects.get(0);
		} 
		
		if (leftY == rightY) {
			// Parabolas are exactly side by side. There is only one intersect, 
			// the desired left/right relationship may not exist.
			if (arcLeft.site.getX() < arcRight.site.getX()) {
				if (intersects.size() != 1) throw new RuntimeException("Seemingly impossible intersection result");
				return intersects.get(0);
			} else {
				throw new RuntimeException("There is no such breakpoint!");
			}
		}

		// if focii are on different sides of the sweepline, there are no intersections
		if (leftY < sweeplineY && rightY > sweeplineY) return null;
		if (leftY > sweeplineY && rightY < sweeplineY) return null;
		
		if (leftY > rightY) {
			// The left parabola is steeper than right. There are 2 intersections between them, but 
			// the X+ most intersect (index: 1) is the intersect for which the desired left/right relationship is correct
			if (intersects.size() != 2) throw new RuntimeException("Seemingly impossible intersection result");
			return intersects.get(1);
		} else {
			// The right parabola is steeper than left. There are 2 intersections between them, but 
			// the X- most intersect (index: 0) is the intersect for which the desired left/right relationship is correct
			if (intersects.size() != 2) throw new RuntimeException("Seemingly impossible intersection result");
			return intersects.get(0);
		}
	}

	@Override
	public Arc getArc(BuildState state, double x) {
		// Call down the tree based on breakpoint positions
		Point2D bp = this.getPosition(state.getSweeplineY());
		if (x <= bp.getX()) {
			return getLeftChild().getArc(state, x);
		} else {
			return getRightChild().getArc(state, x);
		}
	}
	
}
