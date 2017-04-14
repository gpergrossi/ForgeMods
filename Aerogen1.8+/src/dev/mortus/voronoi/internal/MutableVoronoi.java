package dev.mortus.voronoi.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.mortus.util.math.geom.Rect;
import dev.mortus.util.math.geom.Vec2;
import dev.mortus.voronoi.diagram.Edge;
import dev.mortus.voronoi.diagram.Site;
import dev.mortus.voronoi.diagram.Vertex;
import dev.mortus.voronoi.diagram.Voronoi;

public class MutableVoronoi extends Voronoi {

	MutableVoronoi(Rect bounds) {
		this.bounds = bounds;
	}

	private void setSites(Map<Vec2, Site> sites) {
		this.sites = Collections.unmodifiableMap(sites);
	}

	void setMutableSites(Map<Vec2, MutableSite> mutableSites) {
		Map<Vec2, Site> sites = new HashMap<Vec2, Site>(mutableSites.size());
		for (Vec2 pt : mutableSites.keySet()) {
			sites.put(pt, mutableSites.get(pt));
		}
		setSites(sites);
	}

	private void setVertices(List<Vertex> vertices) {
		this.vertices = Collections.unmodifiableList(vertices);
	}

	void setMutableVertices(Collection<MutableVertex> mutableVertices) {
		List<Vertex> vertices = new ArrayList<Vertex>(mutableVertices.size());
		for (MutableVertex v : mutableVertices) {
			vertices.add((Vertex) v);
		}
		setVertices(vertices);
	}

	private void setEdges(List<Edge> edges) {
		this.edges = Collections.unmodifiableList(edges);
	}

	void setMutableEdges(Collection<MutableEdge> mutableEdges) {
		List<Edge> edges = new ArrayList<Edge>(mutableEdges.size());
		for (MutableEdge e : mutableEdges) {
			edges.add((Edge) e);
		}
		setEdges(edges);
	}

}
