package dev.mortus.voronoi.internal;

import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.internal.MathUtil.Parabola;
import dev.mortus.voronoi.internal.MathUtil.Vec2;
import dev.mortus.voronoi.internal.ShoreTree.Node;
import dev.mortus.voronoi.internal.ShoreTree.Node.Type;

/**
 * Arcs are parabolas formed from a site as a focus point and the
 * sweep line as a directrix. The Parabola class used by all Arcs
 * for purposes of calculation is immutable. Therefore, the arc
 * class serves the purpose of forming appropriate Parabolas for a 
 * site at any given position of the sweep line.
 * 
 * @author Gregary
 */
public class Arc extends TreeNode {
	
	public final Site site;
	public Event circleEvent;
	
	public Arc(Site site) {
		super();
		this.site = site;
		this.circleEvent = null;
	}
	
	public void setCircleEvent(Event circleEvent) {
		if (circleEvent != null && circleEvent.type != Event.Type.CIRCLE) {
			throw new RuntimeException("Event is not a Circle Event!");
		}
		this.circleEvent = circleEvent;
	}
	
	public Event getCircleEvent() {
		return circleEvent;
	}
	
	public Parabola getParabola(double sweeplineY) {
		return Parabola.fromPointAndLine(new Vec2(site.getPos()), sweeplineY);
	}

	@Override
	public String getType() {
		return "Arc";
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Arc getArc(BuildState state, double x) {
		// if the getArc call makes it to an Arc (leaf node)
		// return that leaf node.
		return this;
	}

	public Arc insertArc(BuildState state, Site site) {
		TreeNode newBreakpoint = null;
		Arc newArc = null;
		
		if (this.site.getY() == site.getY()) {
			// Y coordinates equal, single breakpoint between sites
			// new arc has greater X coordinate because it came from
			// a priority queue that ensures so
			Arc left = new Arc(this.site);
			Arc right = newArc = new Arc(site);
			newBreakpoint = new Breakpoint(left, right);
			
			newBreakpoint.setLeftChild(left);
			newBreakpoint.setRightChild(right);
		} else {
			// Normal site creation, two breakpoints around new arc
			Arc left = new Arc(this.site);
			Arc right = new Arc(this.site);
			Arc middle = newArc = new Arc(site);
			Breakpoint leftBP = new Breakpoint(oldArc, newArc);
			Breakpoint rightBP = new Breakpoint(newArc, oldArc);

			this.breakpoint = leftBP;
			this.leftChild = new Node(this, oldArc);
			this.rightChild = new Node(this, rightBP);
			newNode = this.rightChild.leftChild;
		}
		
		this.type = Type.Breakpoint;
		this.arc = null;
		
		return newArc;
	}
	
}
		