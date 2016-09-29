package dev.mortus.voronoi.internal.tree;

import dev.mortus.util.data.Pair;
import dev.mortus.util.math.Parabola;
import dev.mortus.util.math.Ray;
import dev.mortus.util.math.Vec2;
import dev.mortus.voronoi.Edge;
import dev.mortus.voronoi.Vertex;
import dev.mortus.voronoi.Voronoi;
import dev.mortus.voronoi.internal.BuildState;
import dev.mortus.voronoi.internal.HalfEdge;

public class Breakpoint extends TreeNode {
	
	public Edge edge;
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
	
	public Vec2 getPosition(final BuildState state) {
		if (state.getSweeplineY() != lastRequest) {
			lastRequest = state.getSweeplineY();
			lastResult = calculatePosition(state.getSweeplineY());
		}
		if (lastResult == null) {
			double x = (arcLeft.site.getX() + arcRight.site.getX()) / 2.0;
			double y = state.getBounds().getMinY()-10;
			lastResult = new Vec2(x, y);
		}
		return lastResult;
	}
	
	/**
	 * Gets the direction this breakpoint moves as the sweepline progresses.
	 * A point is returned representing a vector. The length is not normalized.
	 * @return
	 */
	public Vec2 getDirection() {
		double dy = arcRight.site.getY() - arcLeft.site.getY();
		double dx = arcRight.site.getX() - arcLeft.site.getX();
		
		if (dy == 0) {
			if (dx == 0) return new Vec2(0, 0);
			return new Vec2(0, 1);
		}
		if (dy < 0) {
			return new Vec2(1, -dx/dy);
		} else {
			return new Vec2(-1, dx/dy);
		}
	}
	
	public static Vec2 getIntersection(final BuildState state, Breakpoint left, Breakpoint right) {		
		Vec2 pos0 = left.getPosition(state);
		Vec2 pos1 = right.getPosition(state);
		
		if (pos0 == null || pos1 == null) {
			if (Voronoi.DEBUG) {
				if (pos0 == null) System.err.println("breakpoint position undefined: "+left);
				if (pos1 == null) System.err.println("breakpoint position undefined: "+right);
			}
			return null;
		}
		
		Vec2 dir0 = left.getDirection();
		Vec2 dir1 = right.getDirection();
		
		Ray ray0 = new Ray(pos0, dir0).extend(Voronoi.VERY_SMALL);
		Ray ray1 = new Ray(pos1, dir1).extend(Voronoi.VERY_SMALL);
		
		Vec2 intersection = ray0.intersect(ray1);
		if (intersection == null) {
			if (Voronoi.DEBUG) System.out.println("No intersection between "+left+" and "+right);
			return null;
		}
		
		if (intersection.subtract(pos0).length() < Voronoi.VERY_SMALL) {
			// special case, the breakpoints are currently intersecting: 
			// return null only if they are diverging with respect to Y+
			if (dir0.x < dir1.x) {
				if (Voronoi.DEBUG) System.out.println("Currently intersecting. Diverging (left.dx="+dir0.x+", right.dx="+dir1.x+"). Special case denied.");
				return null;
			} else if (Voronoi.DEBUG) System.out.println("Currently intersecting. Converging (left.dx="+dir0.x+", right.dx="+dir1.x+"). Special case accepted.");
		}
		
		return intersection;
	}

	private Vec2 calculatePosition(double sweeplineY) {
		Parabola leftParabola = arcLeft.getParabola(sweeplineY);
		Parabola rightParabola = arcRight.getParabola(sweeplineY);
		
		Pair<Vec2> intersects = leftParabola.intersect(rightParabola);
		double leftY = arcLeft.site.getY();
		double rightY = arcRight.site.getY();
			
		// Case either parabola is a vertical line (focus y coord = directrix y coord)
		if (leftParabola.isVertical) {
			if (rightParabola.isVertical) return null; // Both parabolas are vertical, no valid intersection
			if (intersects.second != null) throw new RuntimeException("There should only be one intersect in this situation");
			return intersects.first;
		} else if (rightParabola.isVertical) {
			if (intersects.second != null) throw new RuntimeException("There should only be one intersect in this situation");
			return intersects.first;
		} 
		
		if (leftY == rightY) {
			// Parabolas are exactly side by side. There is only one intersect, 
			// the desired left/right relationship may not exist.
			if (arcLeft.site.getX() < arcRight.site.getX()) {
				if (intersects.second != null) throw new RuntimeException("There should only be one intersect in this situation");
				return intersects.first;
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
			if (intersects.first == null) throw new RuntimeException("There should be 2 intersections in this situation");
			return intersects.second;
		} else {
			// The right parabola is steeper than left. There are 2 intersections between them, but 
			// the X- most intersect (index: 0) is the intersect for which the desired left/right relationship is correct
			if (intersects.second == null) throw new RuntimeException("There should be 2 intersections in this situation");
			return intersects.first;
		}
	}

	@Override
	public Arc getArc(final BuildState state, double siteX) {
		// Call down the tree based on breakpoint positions
		Vec2 pos = this.getPosition(state);
		
		double posX;
		if (pos == null) posX = (this.arcLeft.site.getX() + this.arcRight.site.getX()) / 2.0;
		else posX = pos.x;
				
		if (siteX <= posX) {
			if (Voronoi.DEBUG) System.out.println("X:"+siteX+" <= "+this);
			return getLeftChild().getArc(state, siteX);
		} else {
			if (Voronoi.DEBUG) System.out.println("X:"+siteX+" > "+this);
			return getRightChild().getArc(state, siteX);
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

	/**
	 * Checks if this breakpoint has an edge, if not one is created.
	 * The created edge will begin at the current position using
	 * a shared vertex if it is provided. Otherwise, it will create a 
	 * new vertex. 
	 * @param sweeplineY
	 * @param voronoiBounds
	 * @param shared - a vertex that has already been created
	 * @param half - is this a halfEdge? formed by a site event?
	 * @return
	 */
	public Edge checkNewEdge(final BuildState state, Vertex shared, boolean half) {
		if (this.edge != null) return null;
		
		// Get current position
		Vec2 currentPosition = this.getPosition(state);
		if (currentPosition == null) {
			double x = (arcLeft.site.getX() + arcRight.site.getX()) / 2.0;
			currentPosition = new Vec2(x, state.getBounds().getMinY() - 10);
		}
		
		// Find or create a vertex
		Vertex vertex = null;
		if (shared != null) vertex = shared;
		else vertex = new Vertex(currentPosition);
		
		// Create edge
		if (half) this.edge = new HalfEdge(this);
		else this.edge = new Edge(this);
		this.edge.start(vertex);
		return edge;
	}
	
}
