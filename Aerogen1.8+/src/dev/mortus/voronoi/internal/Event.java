package dev.mortus.voronoi.internal;

import dev.mortus.util.math.Circle;
import dev.mortus.util.math.Vec2;
import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.internal.tree.Arc;

public final class Event {

	public static enum Type {
		SITE, CIRCLE;
	}
	
	public final Type type;
	public final Vec2 position;
	public final Circle circle;
	
	public final Site site;
	public final Arc arc;
	
	public static Event createSiteEvent(Site site) {
		return new Event(site);
	}
	
	public static Event createCircleEvent(Arc arc, Circle circle) {
		return new Event(arc, circle);
	}
	
	private Event(Site site) {
		this.type = Type.SITE;
		this.position = site.pos;
		this.site = site;
		this.arc = null;
		this.circle = null;
	}

	private Event(Arc arc, Circle circle) {
		this.type = Type.CIRCLE;
		this.position = new Vec2(Double.NEGATIVE_INFINITY, circle.y + circle.radius);
		this.site = arc.site;
		this.arc = arc;
		this.circle = circle;
	}

	public Vec2 getPos() {
		return position;
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
	
	@Override
	public String toString() {
		if (this.type == Type.CIRCLE) {
			return "Event[Type='Circle', "+arc+", "+circle+"]";
		} else {
			return "Event[Type='Site', "+site+"]";
		}
	}
	
}