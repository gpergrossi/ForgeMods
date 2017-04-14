package dev.mortus.voronoi.diagram;

import dev.mortus.util.data.Pair;
import dev.mortus.util.math.geom.LineSeg;
import dev.mortus.util.math.geom.Vec2;

public class Edge implements Comparable<Edge> {
	
	protected Pair<Site> sites;
	protected Pair<Vertex> vertices;
	private Vec2 center;
	
	protected Edge(Vertex start, Vertex end, Site left, Site right) {
		this.vertices = new Pair<Vertex>(start, end);
		this.sites = new Pair<Site>(left, right);
	}
	
	public LineSeg toLineSeg() {
		return new LineSeg(vertices.first.toVec2(), vertices.second.toVec2());
	}
	
	public Vec2 getCenter() {
		if (center == null) {
			Vec2 v0 = vertices.first.toVec2();
			Vec2 v1 = vertices.second.toVec2();
			center = v0.add(v1).divide(2.0);
		}
		return center;
	}
	
	public Vertex getStart() {
		return vertices.first;
	}
	
	public Vertex getEnd() {
		return vertices.second;
	}
	
	public Pair<Site> getSites() {
		return sites;
	}

	@Override
	public int compareTo(Edge o) {
		return this.hashCode() - o.hashCode();
	}
	
}
