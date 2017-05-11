package dev.mortus.voronoi;

import dev.mortus.util.data.Pair;
import dev.mortus.util.math.func.Function;
import dev.mortus.util.math.func.Quadratic;
import dev.mortus.util.math.geom.Circle;
import dev.mortus.util.math.geom.Vec2;

/**
 * Arcs are parabolas formed from a site as a focus point and the
 * sweep line as a directrix. The Parabola class -- used by all Arcs
 * for purposes of calculation -- is immutable. Therefore, the arc
 * class serves the purpose of forming appropriate Parabolas for a 
 * site at any given position of the sweep line. It also holds onto 
 * important information about the arc, such as its circle event
 * and neighbors.
 */
public class ShoreArc extends ShoreTreeNode {
	
	public final Site site;
	public Event circleEvent;
	
	protected ShoreArc(ShoreTree rootParent, Site site) {
		super(rootParent);
		this.site = site;
		this.circleEvent = null;
	}
	
	public ShoreArc(Site site) {
		super();
		this.site = site;
		this.circleEvent = null;
	}
	
	private void setCircleEvent(Event circleEvent) {
		if (circleEvent != null && circleEvent.type != Event.Type.CIRCLE) {
			throw new RuntimeException("Event is not a Circle Event!");
		}
		this.circleEvent = circleEvent;
	}
	
	public Event getCircleEvent() {
		return circleEvent;
	}
	
	public Function getParabola(double sweeplineY) {
		return Quadratic.fromPointAndLine(site.x, site.y, sweeplineY);
	}

	@Override
	public ShoreArc getArc(final BuildState state, double siteX) {
		return this;
	}

	@Override
	public ShoreBreakpoint getPredecessor() {
		return (ShoreBreakpoint) super.getPredecessor();
	}

	@Override
	public ShoreBreakpoint getSuccessor() {
		return (ShoreBreakpoint) super.getSuccessor();
	}
	
	public Pair<ShoreBreakpoint> getBreakpoints() {
		return new Pair<ShoreBreakpoint>(getPredecessor(), getSuccessor());
	}
	
	public Pair<ShoreArc> getNeighborArcs() {
		return new Pair<ShoreArc>(getLeftNeighborArc(), getRightNeighborArc());
	}
	
	public ShoreArc getLeftNeighborArc() {
		if (this.getPredecessor() == null) return null;
		ShoreArc neighbor = (ShoreArc) this.getPredecessor().getPredecessor();
		return neighbor;
	}
	
	public ShoreArc getRightNeighborArc() {
		if (this.getSuccessor() == null) return null;
		ShoreArc neighbor = (ShoreArc) this.getSuccessor().getSuccessor();
		return neighbor;
	}

	public Event checkCircleEvent(final BuildState state) {
		this.setCircleEvent(null);

		ShoreArc leftNeighbor = this.getLeftNeighborArc();
		ShoreArc rightNeighbor = this.getRightNeighborArc();
		if (leftNeighbor == null || rightNeighbor == null) return null;
		if (leftNeighbor.site == rightNeighbor.site) return null;
		
		ShoreBreakpoint leftBP = (ShoreBreakpoint) getPredecessor();
		ShoreBreakpoint rightBP = (ShoreBreakpoint) getSuccessor();
		Vec2 intersection = ShoreBreakpoint.getIntersection(state, leftBP, rightBP);
		if (intersection == null) return null;
		
		Circle circle = Circle.fromPoints(leftNeighbor.site.toVec2(), this.site.toVec2(), rightNeighbor.site.toVec2());
		if (circle == null) return null;
		
//		if (circle.y() + circle.radius() + Vec2.EPSILON >= state.getSweeplineY()) {
			Event circleEvent = Event.createCircleEvent(this, circle);
			this.setCircleEvent(circleEvent);
			return circleEvent;
//		} else {
//			double dy = circle.y() + circle.radius() + Vec2.EPSILON - state.getSweeplineY();
//			System.out.println("New circle event was above sweepline by "+dy);
//		}
//		
//		return null;
	}
	
	public ShoreArc insertArc(BuildState state, Site site) {
		ShoreBreakpoint newBreakpoint = null;
		ShoreArc newArc = null;
		ShoreArc leftArc = null;
		ShoreArc rightArc = null;
		
		if (Math.abs(this.site.y - site.y) < Vec2.EPSILON) {
			// Y coordinates equal, single breakpoint between sites
			// TODO it bugs me that there is an assumption about the X-order of the sites being made, based on the event order, however this method should have no knowledge of that
			leftArc = new ShoreArc(this.site);
			rightArc = newArc = new ShoreArc(site);
			if (this.site.x > site.x) {
				ShoreArc swap = leftArc;
				leftArc = rightArc;
				rightArc = swap;
			}
			newBreakpoint = new ShoreBreakpoint(leftArc, rightArc);
		} else {
			// Normal site creation, two breakpoints around new arc
			leftArc = new ShoreArc(this.site);
			rightArc = new ShoreArc(this.site);
			ShoreArc middle = newArc = new ShoreArc(site);
			ShoreBreakpoint rightBP = new ShoreBreakpoint(middle, rightArc);
			ShoreBreakpoint leftBP = new ShoreBreakpoint(leftArc, rightBP);
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
		