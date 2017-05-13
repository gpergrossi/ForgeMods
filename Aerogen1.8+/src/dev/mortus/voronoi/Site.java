package dev.mortus.voronoi;

import java.util.Arrays;
import java.util.function.IntFunction;

import dev.mortus.util.math.geom.Polygon;
import dev.mortus.util.math.geom.Vec2;

public class Site implements Comparable<Site> {

	public final Voronoi voronoi;
	protected int id;
	public final double x, y;
	
	protected int numVertices;
	protected Vertex[] vertices;
	protected int numEdges;
	protected Edge[] edges;
	
	protected Polygon polygon;
	
	protected boolean isFinished;
	protected boolean isClosed;

	protected Site(Voronoi voronoi, int id, double x, double y) {
		this.voronoi = voronoi;
		this.id = id;
		this.x = x;
		this.y = y;
		this.edges = new Edge[8];
		this.vertices = new Vertex[8];
	}

	public int numVertices() {
		return numVertices;
	}
	
	public Vertex[] getVertices() {
		return vertices;
	}
	
	public int numEdges() {
		return numEdges;
	}
	
	public Edge[] getEdges() {
		return edges;
	}
	
	private static final IntFunction<Vec2[]> Vec2ArrayAllocator = new IntFunction<Vec2[]>() {
		public Vec2[] apply(int value) {
			return new Vec2[value];
		}
	};
	
	public Polygon getPolygon() {
		if (polygon == null) {
			Vec2[] verts = Arrays.stream(vertices, 0, numVertices).map(vert -> vert.toVec2()).toArray(Vec2ArrayAllocator);
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
		return new Vec2(x, y);
	}
	
	public boolean isClosed() {
		if (isFinished) return isClosed;
		if (vertices == null) return false;
		for (Vertex v : vertices) {
			if (v.isBoundary) return false;
		}
		return true;
	}
	
	boolean hasVertex(Vertex vertex) {
		for (int i = 0; i < numVertices; i++) {
			if (vertices[i] == vertex) return true;
		}
		return false;
	}

	void addVertex(Vertex vertex) {
		if (numVertices >= vertices.length) this.vertices = Arrays.copyOf(vertices, vertices.length*2);
		vertices[numVertices++] = vertex;
	}

	boolean hasEdge(Edge edge) {
		for (int i = 0; i < numEdges; i++) {
			if (edges[i] == edge) return true;
		}
		return false;
	}
	
	void addEdge(Edge edge) {
		if (numEdges >= edges.length) this.edges = Arrays.copyOf(edges, edges.length*2);
		edges[numEdges++] = edge;
	}

	void sortVertices(double[] vertexValues) {
		sort(vertices, 0, numVertices, vertexValues);
	}

	void sortEdges(double[] edgeValues) {
		sort(edges, 0, numEdges, edgeValues);
	}

	/**
	 * A simple selection sort, most site arrays are only < 8 items long
	 */
	private <T> void sort(T[] array, int start, int end, double[] values) {
		for (int i = start; i < end; i++) {
			int bestIndex = i;
			double bestValue = values[i];
			
			// Select best value in sub array from i to end
			for (int j = i+1; j < end; j++) {
				if (values[j] < bestValue) {
					bestIndex = j;
					bestValue = values[j];
				}
			}
			
			// Swap if not the same index
			if (bestIndex != i) {
				T swap = array[i];
				array[i] = array[bestIndex];
				array[bestIndex] = swap;
				values[bestIndex] = values[i];
				values[i] = bestValue;
			}
		}
	}
	
	Vertex getLastVertex() {
		if (numVertices == 0) return null;
		return vertices[numVertices-1];
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

	public int getID() {
		return id;
	}

}