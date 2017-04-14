package dev.mortus.voronoi.diagram;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

import dev.mortus.util.math.geom.Polygon;
import dev.mortus.util.math.geom.Vec2;

public class Site implements Comparable<Site> {

	public final Voronoi voronoi;
	public final int id;
	public final double x, y;
	
	protected boolean isClosed;
	protected List<Vertex> vertices;
	protected List<Edge> edges;
	protected Polygon polygon;

	protected Site (Voronoi voronoi, int id, double x, double y) {
		this.voronoi = voronoi;
		this.id = id;
		this.x = x;
		this.y = y;
		this.edges = new ArrayList<>();
		this.vertices = new ArrayList<>();
	}
	
	public boolean isClosed() {
		return isClosed;
	}
	
	@Override
	public String toString() {
		return "Site[ID="+id+", X="+x+", Y="+y+"]";
	}

	@Override
	public int compareTo(Site o) {		
		if (y < o.y) return -1;
		if (y > o.y) return 1;
		
		if (x < o.x) return -1;
		if (x > o.x) return 1;
		
		return Integer.compare(this.hashCode(), o.hashCode());
	}

	public List<Vertex> getVertices() {
		return vertices;
	}
	
	public List<Edge> getEdges() {
		return edges;
	}
	
	private static final IntFunction<Vec2[]> vec2ArrayCollector = new IntFunction<Vec2[]>() {
		@Override
		public Vec2[] apply(int value) {
			return new Vec2[value];
		}
	};
	
	public Polygon getPolygon() {
		if (polygon == null) {
			Vec2[] verts = getVertices().stream().map(vert -> Vec2.create(vert.x, vert.y)).toArray(vec2ArrayCollector);
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
	
}