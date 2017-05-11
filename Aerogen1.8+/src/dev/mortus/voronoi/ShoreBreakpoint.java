package dev.mortus.voronoi;

import dev.mortus.util.data.LinkedBinaryNode;
import dev.mortus.util.math.func.Function;
import dev.mortus.util.math.func.Quadratic;
import dev.mortus.util.math.geom.Circle;
import dev.mortus.util.math.geom.Vec2;

public class ShoreBreakpoint extends ShoreTreeNode {
	
	public Edge edge;
	protected ShoreArc arcLeft, arcRight;
	
	public ShoreBreakpoint(ShoreTreeNode left, ShoreTreeNode right) {
		if (left.equals(right)) throw new RuntimeException("Cannot construct breakpoint between identical arcs!");
		
		setLeftChild(left);
		setRightChild(right);
		
		checkPossible();
	}

	private void setArcLeft(ShoreArc arc) {
		this.arcLeft = arc;
		if (this.edge != null) this.edge.sites.first = arc.site;
	}

	private void setArcRight(ShoreArc arc) {
		this.arcRight = arc;
		if (this.edge != null) this.edge.sites.second = arc.site;
	}

	@Override
	protected void setLeftChild(LinkedBinaryNode leftBinaryNode) {
		if (leftBinaryNode == null) throw new RuntimeException("Cannot set child of a breakpoint to null");	
		ShoreTreeNode left = (ShoreTreeNode) leftBinaryNode;
		
		super.setLeftChild(left);
		
		// Update arcLeft
		this.setArcLeft((ShoreArc) left.getLastDescendant());
				
		// Update arcRight
		ShoreArc firstArc = (ShoreArc) left.getFirstDescendant();
		ShoreBreakpoint preBreakpoint = firstArc.getPredecessor();
		if (preBreakpoint != null) preBreakpoint.setArcRight(firstArc);
	}
	
	@Override
	protected void setRightChild(LinkedBinaryNode rightBinaryNode) {
		if (rightBinaryNode == null) throw new RuntimeException("Cannot set child of a breakpoint to null");
		ShoreTreeNode right = (ShoreTreeNode) rightBinaryNode;
		
		super.setRightChild(right);
		
		// Update arcRight
		this.setArcRight((ShoreArc) right.getFirstDescendant());

		// Update arcLeft
		ShoreArc lastArc = (ShoreArc) right.getLastDescendant();
		ShoreBreakpoint postBreakpoint = lastArc.getSuccessor();
		if (postBreakpoint != null) postBreakpoint.setArcLeft(lastArc);
	}
	
	private void checkPossible() {
		if ( arcLeft.site.y == arcRight.site.y &&  arcLeft.site.x > arcRight.site.x) {
			// The parabolas are exactly side by side, there is only one intersection between
			// them and the X coordinates of the parabola's focii are in the wrong order for
			// the requested breakpoint to exist.
			throw new RuntimeException("There is no such breakpoint!");
		}
	}
	
	/**
	 * Gets the direction this breakpoint moves as the sweepline progresses.
	 * A point is returned representing a vector. The length is not normalized.
	 * @return
	 */
	public Vec2 getDirection() {
		double dy = arcRight.site.y - arcLeft.site.y;
		double dx = arcRight.site.x - arcLeft.site.x;
		
		if (Math.abs(dy) < Vec2.EPSILON) {
			if (Math.abs(dx) < Vec2.EPSILON) return new Vec2(0, 0);
			return new Vec2(0, 1);
		}
		if (dy < 0) {
			return new Vec2(1, -dx/dy);
		} else {
			return new Vec2(-1, dx/dy);
		}
	}
	
	public static Vec2 getIntersection(final BuildState state, ShoreBreakpoint left, ShoreBreakpoint right) {	
		if (left.arcRight != right.arcLeft) {
			System.out.println("ERROR: expected a shared site between breakpoints! (left.arcRight="+left.arcRight+", right.arcLeft="+right.arcLeft+")");
			return null;
		}

		Vec2 ptLeft = left.arcLeft.site.toVec2();
		Vec2 ptCenter = left.arcRight.site.toVec2();
		Vec2 ptRight = right.arcRight.site.toVec2();
		
		// Check if these breakpoints diverge
		Vec2 dirL = left.getDirection();
		Vec2 dirR = right.getDirection();
		
		if (Voronoi.DEBUG) {
			System.out.println("Checking intersect on");
			System.out.println("left:  "+left);
			System.out.println("       pos:"+left.getPosition(state)+" dir:"+dirL);
			System.out.println("right: "+right);
			System.out.println("       pos:"+right.getPosition(state)+" dir:"+dirR);
		}
		
		Vec2 delta = ptRight.copy();
		delta.subtract(ptLeft);
		double r = dirR.dot(delta); // positive if right breakpoint is moving to the "right" (this is based on the delta vector)
		double l = dirL.dot(delta); // positive if left breakpoint is moving to the "left" (this is based on the delta vector)
		if (r > l) {
			if (Voronoi.DEBUG) System.out.println("Diverging: r="+r+", l="+l);
			return null; // Diverging
		}
		
		// Where would the breakpoints between these sites intersect (if they did)?
		Circle circle = Circle.fromPoints(ptLeft, ptCenter, ptRight);
		if (circle == null) {
			if (Voronoi.DEBUG) System.out.println("Co-linear");
			return null; // sites are co-linear
		}

		Vec2 result = new Vec2(circle.x(), circle.y());
		if (Voronoi.DEBUG) System.out.println("Collision at "+result);
		return result;
	}

	private double lastRequest = Double.NaN;
	private Vec2 lastResult = null;
	
	public Vec2 getPosition(final BuildState state) {
		if (state.getSweeplineY()  != lastRequest) {
			lastRequest = state.getSweeplineY();
			lastResult = calculatePosition(lastRequest);
		}
		if (lastResult == null) {
			// null occurs when sites are on the same y value and have no intersection of their "parabolas"
			double x = (arcLeft.site.x + arcRight.site.x) / 2.0;
			double y = state.getBounds().minY()-50000; // TODO this should actually be a backwards intersection to the top boundary, not an average position
			lastResult = new Vec2(x, y);
		}
		return lastResult;
	}
	
	private Vec2 calculatePosition(double sweeplineY) {		
		Function leftParabola = arcLeft.getParabola(sweeplineY);
		Function rightParabola = arcRight.getParabola(sweeplineY);
		
		return Quadratic.getIntersect(leftParabola, rightParabola);
	}

	@Override
	public ShoreArc getArc(final BuildState state, double siteX) {
		// Call down the tree based on breakpoint positions
		Vec2 pos = this.getPosition(state);
		
		double posX;
		if (pos == null) posX = (this.arcLeft.site.x + this.arcRight.site.x) / 2.0;
		else posX = pos.x();
				
		if (siteX <= posX) {
			if (Voronoi.DEBUG) System.out.println("X:"+siteX+" <= "+this);
			return getLeftChild().getArc(state, siteX);
		} else {
			if (Voronoi.DEBUG) System.out.println("X:"+siteX+" > "+this);
			return getRightChild().getArc(state, siteX);
		}
	}

	public void updateArcs() {
		this.arcLeft = (ShoreArc) this.getLeftChild().getLastDescendant();
		this.arcRight = (ShoreArc) this.getRightChild().getFirstDescendant();
	}	
	
	@Override
	public String toString() {
		String leftID = (hasLeftChild() ? ""+getLeftChild().id : "null");
		String rightID = (hasRightChild() ? ""+getRightChild().id : "null");
		return "Breakpoint["+(debugName != null ? "DebugName='"+debugName+"', " : "")+"ID="+id+", "
				+ "LeftArc="+arcLeft+", RightArc="+arcRight+", "
				+ "Children:[Left="+leftID+", Right="+rightID+"]]";
	}
	
	public ShoreArc getArcLeft() {
		return this.arcLeft;
	}
	
	public ShoreArc getArcRight() {
		return this.arcRight;
	}

	public void setEdge(Edge e) {
		edge = e;
	}
	
	public Edge getEdge() {
		return edge;
	}
	
}
