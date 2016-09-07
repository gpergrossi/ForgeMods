package dev.mortus.voronoi.internal;

import java.awt.geom.Point2D;

import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.internal.MathUtil.Circle;
import dev.mortus.voronoi.internal.tree.Arc;

public final class Event {

	public static enum Type {
		SITE, CIRCLE;
	}
	
	public final Type type;
	public final Point2D point;
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
		this.point = new Point2D.Double(site.getX(), site.getY());
		this.site = site;
		this.arc = null;
		this.circle = null;
	}

	private Event(Arc arc, Circle circle) {
		this.type = Type.CIRCLE;
		this.point = new Point2D.Double(Double.NEGATIVE_INFINITY, circle.y + circle.radius);
		this.site = arc.site;
		this.arc = arc;
		this.circle = circle;
	}

	public Point2D getPosition() {
		return point;
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
	
	public boolean equals(Event e) {
		return type == e.type && point.equals(e.point);
	}
	
}