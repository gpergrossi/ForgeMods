package dev.mortus.voronoi.internal.tree;

import dev.mortus.util.math.func.Function;
import dev.mortus.util.math.func.Quadratic;
import dev.mortus.util.math.geom.Ray;
import dev.mortus.util.math.geom.Vec2;
import dev.mortus.voronoi.diagram.Voronoi;
import dev.mortus.voronoi.internal.BuildState;
import dev.mortus.voronoi.internal.MutableEdge;

public class Breakpoint extends TreeNode {
	
	public MutableEdge edge;
	private Arc arcLeft, arcRight;
	
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
		if ( arcLeft.site.pos.y == arcRight.site.pos.y &&  arcLeft.site.pos.x > arcRight.site.pos.x) {
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
		double dy = arcRight.site.pos.y - arcLeft.site.pos.y;
		double dx = arcRight.site.pos.x - arcLeft.site.pos.x;
		
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

	private double lastRequest = Double.NaN;
	private Vec2 lastResult = null;
	
	public Vec2 getPosition(final BuildState state) {
		if (state.getSweeplineY() != lastRequest) {
			lastRequest = state.getSweeplineY();
			lastResult = calculatePosition(state.getSweeplineY());
		}
		if (lastResult == null) {
			double x = (arcLeft.site.pos.x + arcRight.site.pos.x) / 2.0;
			double y = state.getBounds().minY()-10;
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
	public Arc getArc(final BuildState state, double siteX) {
		// Call down the tree based on breakpoint positions
		Vec2 pos = this.getPosition(state);
		
		double posX;
		if (pos == null) posX = (this.arcLeft.site.pos.x + this.arcRight.site.pos.x) / 2.0;
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
	
	public Arc getArcLeft() {
		return this.arcLeft;
	}
	
	public Arc getArcRight() {
		return this.arcRight;
	}
	
	/**
	 * Checks if this breakpoint has an edge. If not, one is created.
	 * The created edge will begin at the current position using
	 * a shared vertex if it is provided. Otherwise, it will create a 
	 * new vertex. 
	 * @param sweeplineY
	 * @param voronoiBounds
	 * @param shared - a vertex that has already been created
	 * @param half - is this a halfEdge? formed by a site event?
	 * @return
	 */
	/*
	public Edge checkNewEdge(final BuildState state, Vertex shared, boolean half) {
		if (this.edge != null) return null;
		
		// Get current position
		Vec2 currentPosition = this.getPosition(state);
		if (currentPosition == null) {
			double x = (arcLeft.site.pos.x + arcRight.site.pos.x) / 2.0;
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
	*/
	
}
