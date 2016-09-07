package dev.mortus.voronoi.internal.tree;

import java.awt.geom.Point2D;
import java.util.List;

import dev.mortus.voronoi.internal.BuildState;
import dev.mortus.voronoi.internal.MathUtil.Parabola;
import dev.mortus.voronoi.internal.MathUtil.Vec2;

public class Breakpoint extends TreeNode {
	
	public Arc arcLeft, arcRight;
	
	public Breakpoint(TreeNode left, TreeNode right) {
		if (left.equals(right)) throw new RuntimeException("Cannot construct breakpoint between identical arcs!");
		
		setLeftChild(left);
		setRightChild(right);
		
		checkPossible();
	}

	@Override
	protected void setLeftChild(TreeNode left) {
		if (left == null) throw new RuntimeException("Cannot set child of a breakpoint to null");		
		super.setLeftChild(left);
		this.arcLeft = (Arc) this.getLeftChild().getLastDescendant();
	}

	@Override
	protected void setRightChild(TreeNode right) {
		if (right == null) throw new RuntimeException("Cannot set child of a breakpoint to null");		
		super.setRightChild(right);
		this.arcRight = (Arc) this.getRightChild().getFirstDescendant();
	}
	
	private void checkPossible() {		
		if (arcLeft.site.getY() == arcRight.site.getY() && arcLeft.site.getX() > arcRight.site.getX()) {
			// The parabolas are exactly side by side, there is only one intersection between
			// them and the X coordinates of the parabola's focii are in the wrong order for
			// the requested breakpoint to exist.
			throw new RuntimeException("There is no such breakpoint!");
		}
	}

	@Override
	public Type getType() {
		return Type.Breakpoint;
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
	
	public Point2D getDirection() {
		if (lastRequest == Double.NaN) {
			double startY = Math.max(arcLeft.site.getY(), arcRight.site.getX())+1;
			getPosition(startY);
		}
		
		Point2D p0 = getPosition(lastRequest);
		Point2D p1 = calculatePosition(lastRequest+10).toPoint();
		Point2D diff = new Point2D.Double(p1.getX()-p0.getX(), p1.getY()-p0.getY());
		
		return diff;	
	}
	
	public Point2D getIntersection(double sweeplineY, Breakpoint other) {
		Point2D pos0 = getPosition(lastRequest);
		Point2D dir0 = getDirection();
		Point2D pos1 = other.getPosition(other.lastRequest);
		Point2D dir1 = other.getDirection();
		
		double dx = pos1.getX() - pos0.getX();
		double dy = pos1.getY() - pos0.getY();
		double det = dir1.getX() * dir0.getY() - dir1.getY() * dir0.getX();
		
		if (det == 0) return null;
		
		double u = (dy * dir1.getX() - dx * dir1.getY()) / det;
		double v = (dy * dir0.getX() - dx * dir0.getY()) / det;
		
		if (u < 0 || v < 0) return null;
		
		return new Point2D.Double(pos0.getX() + dir0.getX()*u, pos0.getY() + dir0.getY()*u);		
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

	public void updateArcs() {
		this.arcLeft = (Arc) this.getLeftChild().getLastDescendant();
		this.arcRight = (Arc) this.getRightChild().getFirstDescendant();
	}	
	
}
