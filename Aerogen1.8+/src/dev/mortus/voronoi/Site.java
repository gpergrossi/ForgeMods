package dev.mortus.voronoi;

import java.awt.geom.Point2D;

public class Site {

	private static int IDCounter = 0;
	
	final Point2D.Double position;
	public final int id;

	public Site (Point2D position) {
		this.id = IDCounter++;
		this.position = new Point2D.Double(position.getX(), position.getY());
	}
	
	public Point2D getPos() {
		return position;
	}
	
	public double getX() {
		return position.x;
	}
	
	public double getY() {
		return position.y;
	}
	
	public boolean equals(Site o) {
		if (position.x != o.position.x) return false;
		if (position.y != o.position.y) return false;
		return true;
	}
	
}
