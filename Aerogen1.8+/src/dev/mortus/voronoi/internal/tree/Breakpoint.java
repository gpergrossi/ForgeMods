package dev.mortus.voronoi.internal.tree;

import java.awt.geom.Point2D;
import java.util.List;

import dev.mortus.voronoi.Voronoi;
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

	protected void setLeftChild(TreeNode left) {
		if (left == null) throw new RuntimeException("Cannot set child of a breakpoint to null");		
		super.setLeftChild(left);
		this.arcLeft = (Arc) this.getLeftChild().getLastDescendant();
	}

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
	
	/**
	 * Gets the direction this breakpoint moves as the sweepline progresses.
	 * A point is returned representing a vector. The length is not normalized.
	 * @return
	 */
	private Point2D getDirection() {
		double dy = arcRight.site.getY() - arcLeft.site.getY();
		double dx = arcRight.site.getX() - arcLeft.site.getX();
		
		if (dy == 0) {
			if (dx == 0) return new Point2D.Double(0, 0);
			return new Point2D.Double(0, 1);
		}
		if (dy < 0) {
			return new Point2D.Double(1, -dx/dy);
		} else {
			return new Point2D.Double(-1, dx/dy);
		}
	}
	
	public static Point2D getIntersection(double sweeplineY, Breakpoint left, Breakpoint right) {		
		Point2D pos0 = left.getPosition(sweeplineY);
		Point2D dir0 = left.getDirection();
		Point2D pos1 = right.getPosition(sweeplineY);
		Point2D dir1 = right.getDirection();
		
		if (Voronoi.DEBUG) {
			if (pos0 == null) {
				System.err.println("breakpoint position undefined: "+left);
				return null;
			}
			if (pos1 == null) {
				System.err.println("breakpoint position undefined: "+right);
				return null;
			}
		}
		if (pos0 == null || pos1 == null) return null;
		
		double dx = pos1.getX() - pos0.getX();
		double dy = pos1.getY() - pos0.getY();
		double det = dir1.getX() * dir0.getY() - dir1.getY() * dir0.getX();
		
		if (det == 0) {
			if (Voronoi.DEBUG) System.err.println("determinant of 0");
			return null;
		}
		
		double u = (dy * dir1.getX() - dx * dir1.getY()) / det;
		double v = (dy * dir0.getX() - dx * dir0.getY()) / det;

		if (u < -Voronoi.VERY_SMALL || v < -Voronoi.VERY_SMALL) {
			if (Voronoi.DEBUG) System.err.println("Intersection at negative U or V: "+left+", "+right);
			return null; // intersection is behind the current position of one of the breakpoints
		}
		if (abs(u) < Voronoi.VERY_SMALL && abs(v) < Voronoi.VERY_SMALL) {
			// special case, the breakpoints are currently intersecting: 
			// return null only if they are diverging with respect to Y+
			if (dir0.getX() < dir1.getX()) {
				if (Voronoi.DEBUG) System.out.println("Currently intersecting. Diverging (left.dx="+dir0.getX()+", right.dx="+dir1.getX()+"). Special case denied.");
				return null;
			} else {
				if (Voronoi.DEBUG) System.out.println("Currently intersecting. Converging (left.dx="+dir0.getX()+", right.dx="+dir1.getX()+"). Special case accepted.");
			}
		}
		
		return new Point2D.Double(pos0.getX() + dir0.getX()*u, pos0.getY() + dir0.getY()*u);		
	}
	
	
	private static double abs(double v) {
		if (v < 0) return -v;
		return v;
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
	public Arc getArc(double sweeplineY, double siteX) {
		// Call down the tree based on breakpoint positions
		Point2D bp = this.getPosition(sweeplineY);
		
		double bpx;
		if (bp == null) bpx = (this.arcLeft.site.getX() + this.arcRight.site.getX()) / 2.0;
		else bpx = bp.getX();
		
		if (siteX <= bpx) {
			return getLeftChild().getArc(sweeplineY, siteX);
		} else {
			return getRightChild().getArc(sweeplineY, siteX);
		}
	}

	public void updateArcs() {
		this.arcLeft = (Arc) this.getLeftChild().getLastDescendant();
		this.arcRight = (Arc) this.getRightChild().getFirstDescendant();
	}	
	
	@Override
	public String toString() {
		String leftID = (hasLeftChild() ? ""+getLeftChild().id : "null");
		String rightID = (hasRightChild() ? ""+getRightChild().id : "null");
		return "Breakpoint["+(debugName != null ? "DebugName='"+debugName+"', " : "")+"ID="+id+", "
				+ "LeftArc="+arcLeft+", RightArc="+arcRight+", "
				+ "Children:[Left="+leftID+", Right="+rightID+"]]";
	}
	
}
