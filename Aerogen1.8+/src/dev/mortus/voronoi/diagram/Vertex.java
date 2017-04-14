package dev.mortus.voronoi.diagram;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import dev.mortus.util.math.geom.Vec2;

public class Vertex implements Comparable<Vertex> {

	public static final double VERY_SMALL_2 = Voronoi.VERY_SMALL * Voronoi.VERY_SMALL;

	public final boolean isBoundary;
	public final double x, y;
	
	protected List<Edge> edges;
	protected List<Site> sites;
	
	protected String debug = "";

	protected Vertex(double x, double y, boolean isBoundary) {
		this.x = x;
		this.y = y;
		this.isBoundary = isBoundary;
		this.edges = new ArrayList<Edge>();
		this.sites = new ArrayList<Site>();
	}
	
	protected Vertex(double x, double y) {
		this(x, y, false);
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public boolean isBoundary() {
		return isBoundary;
	}
	
	public String toString() {
		return "Vertex[x="+x+", y="+y+"]";
	}

	@Override
	public int compareTo(Vertex o) {
		if (y < o.y) return -1;
		if (y > o.y) return 1;
		
		if (x < o.x) return -1;
		if (x > o.x) return 1;
		
		return Integer.compare(this.hashCode(), o.hashCode());
	}

	public Point2D toPoint2D() {
		return new Point2D.Double(x, y);
	}

	public Vec2 toVec2() {
		return Vec2.create(x, y);
	}
	
}
