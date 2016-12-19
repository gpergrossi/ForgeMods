package dev.mortus.voronoi.diagram;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import dev.mortus.util.math.geom.Vec2;

public class Vertex implements Comparable<Vertex> {

	public static final double VERY_SMALL_2 = Voronoi.VERY_SMALL * Voronoi.VERY_SMALL;

	final boolean isBoundary;
	final Vec2 position;
	
	protected List<Edge> edges;
	protected List<Site> sites;
	
	protected String debug = "";

	protected Vertex(Vec2 pos, boolean isBoundary) {
		if (pos == null) throw new RuntimeException("null position");
		this.position = pos;
		this.isBoundary = isBoundary;
		this.edges = new ArrayList<Edge>();
		this.sites = new ArrayList<Site>();
	}
	
	protected Vertex(Vec2 pos) {
		this(pos, false);
	}
	
	public Vec2 getPosition() {
		return position;
	}
	
	public Point2D toPoint2D() {
		return position.toPoint2D();
	}
	
	public boolean isBoundary() {
		return isBoundary;
	}
	
	public String toString() {
		return "Vertex[pos="+position+"]";
	}

	@Override
	public int compareTo(Vertex o) {
		return this.position.compareTo(o.position);
	}
	
}
