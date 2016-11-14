package dev.mortus.voronoi.internal.tree;

import dev.mortus.util.data.Pair;
import dev.mortus.util.math.Circle;
import dev.mortus.util.math.Parabola;
import dev.mortus.util.math.Vec2;
import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.Voronoi;
import dev.mortus.voronoi.internal.BuildState;
import dev.mortus.voronoi.internal.Event;

/**
 * Arcs are parabolas formed from a site as a focus point and the
 * sweep line as a directrix. The Parabola class -- used by all Arcs
 * for purposes of calculation -- is immutable. Therefore, the arc
 * class serves the purpose of forming appropriate Parabolas for a 
 * site at any given position of the sweep line. It also holds onto 
 * important information about the arc, such as its circle event
 * and neighbors.
 */
public class Arc extends TreeNode {
	
	public final Site site;
	public Event circleEvent;
	
	protected Arc(ShoreTree rootParent, Site site) {
		super(rootParent);
		this.site = site;
		this.circleEvent = null;
	}
	
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
		return Parabola.fromPointAndLine(site.pos, sweeplineY);
	}

	@Override
	public Arc getArc(final BuildState state, double siteX) {
		return this;
	}

	@Override
	public Breakpoint getPredecessor() {
		return (Breakpoint) super.getPredecessor();
	}

	@Override
	public Breakpoint getSuccessor() {
		return (Breakpoint) super.getSuccessor();
	}
	
	public Pair<Breakpoint> getBreakpoints() {
		return new Pair<Breakpoint>(getPredecessor(), getSuccessor());
	}
	
	public Pair<Arc> getNeighborArcs() {
		return new Pair<Arc>(getLeftNeighborArc(), getRightNeighborArc());
	}
	
	public Arc getLeftNeighborArc() {
		if (this.getPredecessor() == null) return null;
		Arc neighbor = (Arc) this.getPredecessor().getPredecessor();
		return neighbor;
	}
	
	public Arc getRightNeighborArc() {
		if (this.getSuccessor() == null) return null;
		Arc neighbor = (Arc) this.getSuccessor().getSuccessor();
		return neighbor;
	}

	public Event checkCircleEvent(final BuildState state) {
		do {
			Arc leftNeighbor = this.getLeftNeighborArc();
			Arc rightNeighbor = this.getRightNeighborArc();
			if (leftNeighbor == null || rightNeighbor == null) break;
			if (leftNeighbor.site == rightNeighbor.site) break;
			
			Breakpoint leftBP = (Breakpoint) getPredecessor();
			Breakpoint rightBP = (Breakpoint) getSuccessor();
			Vec2 intersection = Breakpoint.getIntersection(state, leftBP, rightBP);
			if (intersection == null) break;
			
			Circle circle = Circle.fromPoints(leftNeighbor.site.pos, this.site.pos, rightNeighbor.site.pos);
			if (circle == null) break;
			
			if (circle.y + circle.radius + Voronoi.VERY_SMALL >= state.getSweeplineY()) {
				Event circleEvent = Event.createCircleEvent(this, circle);
				this.setCircleEvent(circleEvent);
				return circleEvent;
			}
		} while (false);
		
		this.setCircleEvent(null);
		return null;
	}
	
	public Arc insertArc(BuildState state, Site site) {
		Breakpoint newBreakpoint = null;
		Arc newArc = null;
		
		if (this.site.pos.y == site.pos.y) {
			// Y coordinates equal, single breakpoint between sites
			// new arc has greater X coordinate because it came from
			// a priority queue that ensures so
			Arc left = new Arc(this.site);
			Arc right = newArc = new Arc(site);
			newBreakpoint = new Breakpoint(left, right);
			
		} else {
			// Normal site creation, two breakpoints around new arc
			Arc left = new Arc(this.site);
			Arc right = new Arc(this.site);
			Arc middle = newArc = new Arc(site);
			Breakpoint rightBP = new Breakpoint(middle, right);
			Breakpoint leftBP = new Breakpoint(left, rightBP);
			
			newBreakpoint = leftBP;
		}
		
		this.replaceWith(newBreakpoint);
		
		return newArc;
	}

	@Override
	public String toString() {
		return "Arc["+(debugName != null ? "Name='"+debugName+"', " : "")+"ID="+id+", "
				+ "Site="+site.id+", CircleEvent="+(circleEvent!=null)+"]";
	}
	
}
		