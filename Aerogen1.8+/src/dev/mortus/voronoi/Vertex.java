package dev.mortus.voronoi;

import java.awt.geom.Point2D;

import dev.mortus.util.math.Vec2;

public class Vertex {

	public static final double VERY_SMALL_2 = Voronoi.VERY_SMALL * Voronoi.VERY_SMALL;

	final boolean isBoundary;
	final Vec2 position;

	public Vertex(Vec2 pos, boolean isBoundary) {
		this.position = pos;
		this.isBoundary = isBoundary;
	}
	
	public Vertex(Vec2 pos) {
		this.position = pos;
		this.isBoundary = false;
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
	
}
