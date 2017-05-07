package dev.mortus.voronoi.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntFunction;

import dev.mortus.util.math.geom.Polygon;
import dev.mortus.util.math.geom.Vec2;
import dev.mortus.voronoi.diagram.Voronoi;

public class Site implements Comparable<Site> {

	public final Voronoi voronoi;
	public final int id;
	public final double x, y;
	
	protected List<Vertex> vertices;
	protected List<Edge> edges;
	protected Polygon polygon;
	
	protected boolean isFinished;
	protected boolean isClosed;

	protected Site(Voronoi voronoi, int id, double x, double y) {
		this.voronoi = voronoi;
		this.id = id;
		this.x = x;
		this.y = y;
		this.edges = new ArrayList<>();
		this.vertices = new ArrayList<>();
	}

	public List<Vertex> getVertices() {
		return vertices;
	}
	
	public List<Edge> getEdges() {
		return edges;
	}
	
	private static final IntFunction<Vec2[]> Vec2ArrayAllocator = new IntFunction<Vec2[]>() {
		public Vec2[] apply(int value) {
			return new Vec2[value];
		}
	};
	
	public Polygon getPolygon() {
		if (polygon == null) {
			Vec2[] verts = getVertices().stream().map(vert -> vert.toVec2()).toArray(Vec2ArrayAllocator);
			polygon = new Polygon(verts);
		}
		return polygon;
	}

	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}

	public Vec2 toVec2() {
		return Vec2.create(x, y);
	}
	
	public boolean isClosed() {
		if (isFinished) return isClosed;
		if (vertices == null) return false;
		for (Vertex v : vertices) {
			if (v.isBoundary) return false;
		}
		return true;
	}

	void addVertex(Vertex vertex) {
		vertices.add(vertex);
	}

	void addEdge(Edge edge) {
		edges.add(edge);
	}

	void sortVertices(Comparator<Vertex> order) {
		vertices.sort(order);
	}

	void sortEdges(Comparator<Edge> order) {
		edges.sort(order);
	}

	Vertex getLastVertex() {
		if (vertices.size() == 0) return null;
		return (Vertex) vertices.get(vertices.size()-1);
	}

	void makeListsUnmodifiable() {
		edges = Collections.unmodifiableList(edges);
		vertices = Collections.unmodifiableList(vertices);
	}

	@Override
	public int compareTo(Site o) {		
		if (y < o.y) return -1;
		if (y > o.y) return 1;
		
		if (x < o.x) return -1;
		if (x > o.x) return 1;
		
		return Integer.compare(this.hashCode(), o.hashCode());
	}
	
	@Override
	public String toString() {
		return "Site[ID="+id+", X="+x+", Y="+y+"]";
	}

}
