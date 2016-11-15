package dev.mortus.voronoi;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import dev.mortus.util.math.Vec2;

public class Vertex {

	public static final double VERY_SMALL_2 = Voronoi.VERY_SMALL * Voronoi.VERY_SMALL;

	final boolean isBoundary;
	final Vec2 position;
	
	public List<Edge> edges;
	public List<Site> sites;
	
	public String debug = "";

	public Vertex(Vec2 pos, boolean isBoundary) {
		if (pos == null) throw new RuntimeException("null position");
		this.position = pos;
		this.isBoundary = isBoundary;
		this.edges = new ArrayList<Edge>();
		this.sites = new ArrayList<Site>();
	}
	
	public Vertex(Vec2 pos) {
		this(pos, false);
	}

	public boolean isCloseTo(Vec2 other) {
		double dx = (position.x-other.x);
		double dy = (position.y-other.y);
		return (dx*dx + dy*dy) < VERY_SMALL_2;
	}
	
	public Point2D toPoint2D() {
		return position.toPoint2D();
	}
	
	public Vec2 getPosition() {
		return position;
	}
	
	public boolean isBoundary() {
		return isBoundary;
	}
	
	public boolean equals(Vertex other) {
		return this == other;
	}
	
	public String toString() {
		return "Vertex[pos="+position+"]";
	}
	
}
