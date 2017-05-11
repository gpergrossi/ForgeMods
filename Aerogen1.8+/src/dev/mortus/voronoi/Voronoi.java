package dev.mortus.voronoi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.mortus.util.data.storage.GrowingStorage;
import dev.mortus.util.math.geom.Rect;
import dev.mortus.util.math.geom.Vec2;

public class Voronoi {

	/** 
	 * A very small number that is still at a good precision in the floating point
	 * format. All inter-site distances should be much larger than this (> 100x for safety)
	 */
	public static boolean DEBUG = false;
	public static boolean DEBUG_FINISH = false;
	
	protected Rect bounds;
	
	protected Map<Vec2, Site> sites;
	protected List<Edge> edges;
	protected List<Vertex> vertices;
	
	protected Voronoi() {}
	
	Voronoi(Rect bounds) {
		this.bounds = bounds;
	}
	
	public Rect getBounds() {
		return bounds;
	}

	public Map<Vec2, Site> getSites() {
		return sites;
	}
	
	public List<Edge> getEdges() {
		return edges;
	}

	public List<Vertex> getVertices() {
		return vertices;
	}

	public int numSites() {
		return sites.size();
	}

	
	
	private void setSites(Map<Vec2, Site> sites) {
		this.sites = Collections.unmodifiableMap(sites);
	}

	void setMutableSites(Vec2[] locations, Site[] sites) {
		Map<Vec2, Site> map = new HashMap<Vec2, Site>(sites.length);
		for (int i = 0; i < locations.length; i++) {
			map.put(locations[i], sites[i]);
		}
		setSites(map);
	}

	private void setVertices(List<Vertex> vertices) {
		this.vertices = Collections.unmodifiableList(vertices);
	}

	void setMutableVertices(GrowingStorage<Vertex> mutableVertices) {
		List<Vertex> vertices = new ArrayList<Vertex>(mutableVertices.size());
		for (Vertex v : mutableVertices) {
			vertices.add(v);
		}
		setVertices(vertices);
	}

	private void setEdges(List<Edge> edges) {
		this.edges = Collections.unmodifiableList(edges);
	}

	void setMutableEdges(GrowingStorage<Edge> mutableEdges) {
		List<Edge> edges = new ArrayList<Edge>(mutableEdges.size());
		for (Edge e : mutableEdges) {
			edges.add(e);
		}
		setEdges(edges);
	}
	
}
