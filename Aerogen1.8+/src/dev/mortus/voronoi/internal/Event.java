package dev.mortus.voronoi.internal;

import java.awt.geom.Point2D;

import dev.mortus.voronoi.Site;

public final class Event {

	public static enum Type {
		SITE, CIRCLE;
	}
	
	public final Type type;
	public final Point2D point;
	
	public final Site site;
	public final Arc arc;
	
	public Event(Site site) {
		this.type = Type.SITE;
		this.point = new Point2D.Double(site.getX(), site.getY());
		this.site = site;
		this.arc = null;
	}

	public Event(Arc arc) {
		this.type = Type.CIRCLE;
		this.point = new Point2D.Double(10000, 10000); // TODO
		this.site = null;
		this.arc = arc;
	}

	public Point2D getPosition() {
		return point;
	}
	
}