package dev.mortus.voronoi.internal;

import dev.mortus.util.math.geom.Circle;
import dev.mortus.voronoi.exception.OverlappingSiteException;
import dev.mortus.voronoi.internal.shoretree.Arc;

public final class Event implements Comparable<Event> {

	public static enum Type {
		SITE, CIRCLE;
	}
	
	public final Type type;
	public final double x, y;
	public final Circle circle;
	
	public final Site site;
	public final Arc arc;
	
	public boolean valid;
	
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
		int dy = (int) Math.signum(this.y - o.y);
		if (dy != 0) return dy;
		
		// Lowest X value first
		int dx = (int) Math.signum(this.x - o.x);
		if (dx != 0) return dx;
		
		// Allow equal priority circle events
		if (this.type == Type.CIRCLE) return 0;
		if (o.type == Type.CIRCLE) return 0;
		
		// We cannot allow multiple site events with the same exact position
		throw new OverlappingSiteException(this.site, o.site);
	}
	
	
}