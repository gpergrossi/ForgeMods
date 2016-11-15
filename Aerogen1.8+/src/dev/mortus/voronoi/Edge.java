package dev.mortus.voronoi;

import dev.mortus.util.data.Pair;
import dev.mortus.util.math.LineSeg;
import dev.mortus.util.math.Vec2;
import dev.mortus.voronoi.internal.MutableEdge;

public class Edge {
	
	public Pair<Site> sites;
	public Pair<Vertex> vertices;

	public Edge(MutableEdge e) {
		this.sites = e.sites;
		this.vertices = e.vertices;
	}
	
	protected Edge(Vertex start, Vertex end, Site left, Site right) {
		this.vertices = new Pair<Vertex>(start, end);
		this.sites = new Pair<Site>(left, right);
	}

	public LineSeg toLineSeg() {
		if (!isFinished()) return null;
		return new LineSeg(vertices.first.position, vertices.second.position);
	}
	
	public Vec2 getCenter() {
		if (!isFinished()) return null;
		Vec2 v0 = vertices.first.getPosition();
		Vec2 v1 = vertices.second.getPosition();
		return v0.add(v1).divide(2.0);
	}

	public boolean isBoundary() {
		return sites.first == null;
	}
	
	public boolean isFinished() {
		return vertices.size() == 2;
	}
	
	public Vertex start() {
		return vertices.first;
	}
	
	public Vertex end() {
		return vertices.second;
	}
	
}
