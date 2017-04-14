package dev.mortus.voronoi.internal;

import dev.mortus.util.math.geom.Circle;
import dev.mortus.voronoi.diagram.Site;
import dev.mortus.voronoi.exception.OverlappingSiteException;
import dev.mortus.voronoi.internal.tree.Arc;

public final class Event implements Comparable<Event> {

	public static enum Type {
		SITE, CIRCLE;
	}
	
	private final Type type;
	private final double x, y;
	private final Circle circle;
	
	private final Site site;
	private final Arc arc;
	
	private boolean valid;
	
	public static Event createSiteEvent(Site site) {
		return new Event(site);
	}
	
	private Event(Site site) {
		this.type = Type.SITE;
		this.x = site.x;
		this.y = site.y;
		this.site = site;
		this.arc = null;
		this.circle = null;
		this.valid = true;
	}
	
	public static Event createCircleEvent(Arc arc, Circle circle) {
		return new Event(arc, circle);
	}
	
	private Event(Arc arc, Circle circle) {
		this.type = Type.CIRCLE;
		this.x = Double.NEGATIVE_INFINITY;
		this.y = circle.y + circle.radius;
		this.site = arc.site;
		this.arc = arc;
		this.circle = circle;
		this.valid = true;
	}

	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public Site getSite() {
		return site;
	}

	public Arc getArc() {
		return arc;
	}
	
	public Circle getCircle() {
		return circle;
	}

	public boolean is(Type type) {
		return this.type == type;
	}

	public Type getType() {
		return this.type;
	}
	
	@Override
	public String toString() {
		if (this.type == Type.CIRCLE) {
			return "Event[Type='Circle', "+arc+", "+circle+", y="+y+"]";
		} else {
			return "Event[Type='Site', "+site+", x="+x+", y="+y+"]";
		}
	}

	@Override
	public int compareTo(Event o) {
		// Lowest Y value first
		double dy = this.y - o.y;
		if (dy > 0) return 1;
		if (dy < 0) return -1;
		
		// Lowest X value first
		double dx = this.x - o.x;
		if (dx > 0) return 1;
		if (dx < 0) return -1;
		
		// Allow equal priority circle events
		if (this.type == Type.CIRCLE) return 0;
		if (o.type == Type.CIRCLE) return 0;
		
		// We cannot allow multiple site events with the same exact position
		throw new OverlappingSiteException(this.getSite(), o.getSite());
	}

	public boolean isValid() {
		return valid;
	}
	
	public void invalidate() {
		valid = false;
	}
	
	
}