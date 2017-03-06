package dev.mortus.voronoi.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;

import dev.mortus.util.math.geom.Polygon;
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

	private void setSites(List<Site> sites) {
		this.sites = Collections.unmodifiableList(sites);
	}

	void setMutableSites(List<MutableSite> mutableSites) {
		List<Site> sites = new ArrayList<Site>(mutableSites.size());
		for (MutableSite s : mutableSites) {
			sites.add((Site) s);
		}
		setSites(sites);
	}

	private void setVertices(List<Vertex> vertices) {
		this.vertices = Collections.unmodifiableList(vertices);
	}

	void setMutableVertices(List<MutableVertex> mutableVertices) {
		List<Vertex> vertices = new ArrayList<Vertex>(mutableVertices.size());
		for (MutableVertex v : mutableVertices) {
			vertices.add((Vertex) v);
		}
		setVertices(vertices);
	}

	private void setEdges(List<Edge> edges) {
		this.edges = Collections.unmodifiableList(edges);
	}

	void setMutableEdges(List<MutableEdge> mutableEdges) {
		List<Edge> edges = new ArrayList<Edge>(mutableEdges.size());
		for (MutableEdge e : mutableEdges) {
			edges.add((Edge) e);
		}
		setEdges(edges);
	}

	private static final IntFunction<Vec2[]> vec2ArrayCollector = new IntFunction<Vec2[]>() {
		@Override
		public Vec2[] apply(int value) {
			return new Vec2[value];
		}
	};

	public void buildSitePolygons() {
		this.sitePolygons = new ArrayList<>();

		for (Site site : sites) {
			Vec2[] verts = site.getVertices().stream().map(vert -> vert.getPosition()).toArray(vec2ArrayCollector);
			this.sitePolygons.add(new Polygon(verts));
		}

	}

}
