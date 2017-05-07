package dev.mortus.voronoi.diagram;

import java.util.List;
import java.util.Map;

import dev.mortus.util.math.geom.Rect;
import dev.mortus.util.math.geom.Vec2;
import dev.mortus.voronoi.internal.Edge;
import dev.mortus.voronoi.internal.Site;
import dev.mortus.voronoi.internal.Vertex;

public class Voronoi {

	/** 
	 * A very small number that is still at a good precision in the floating point
	 * format. All inter-site distances should be much larger than this (> 100x for safety)
	 */
	public static final double VERY_SMALL = 0.0001;
	public static boolean DEBUG = false;
	public static boolean DEBUG_FINISH = false;
	
	protected Rect bounds;
	
	protected Map<Vec2, Site> sites;
	protected List<Edge> edges;
	protected List<Vertex> vertices;
	
	protected Voronoi() {}
	
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
	
}
