package dev.mortus.voronoi.diagram;

import dev.mortus.util.data.Pair;
import dev.mortus.util.math.geom.LineSeg;
import dev.mortus.util.math.geom.Vec2;

public class Edge implements Comparable<Edge> {
	
	protected final Pair<Site> sites;
	protected final Pair<Vertex> vertices;
	
	protected Edge(Vertex start, Vertex end, Site left, Site right) {
		this.vertices = new Pair<Vertex>(start, end);
		this.sites = new Pair<Site>(left, right);
	}
	
	public LineSeg toLineSeg() {
		return new LineSeg(vertices.first.position, vertices.second.position);
	}
	
	public Vec2 getCenter() {
		Vec2 v0 = vertices.first.getPosition();
		Vec2 v1 = vertices.second.getPosition();
		return v0.add(v1).divide(2.0);
	}

	@Deprecated
	public boolean isBoundary() {
		return sites.first == null;
	}
	
	public Vertex getStart() {
		return vertices.first;
	}
	
	public Vertex getEnd() {
		return vertices.second;
	}

	@Override
	public int compareTo(Edge o) {
		return this.hashCode() - o.hashCode();
	}
	
}
