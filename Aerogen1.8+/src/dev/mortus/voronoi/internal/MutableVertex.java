package dev.mortus.voronoi.internal;

import java.util.Collections;
import java.util.List;

import dev.mortus.util.math.geom.Vec2;
import dev.mortus.voronoi.diagram.Site;
import dev.mortus.voronoi.diagram.Vertex;

public class MutableVertex extends Vertex {

	MutableVertex(Vec2 pos) {
		super(pos);
	}

	MutableVertex(Vec2 pos, boolean isBoundary) {
		super(pos, isBoundary);
	}

	void addEdge(MutableEdge e) {
		this.edges.add(e);
		
	}

	List<Site> getSites() {
		return sites;
	}

	void addSite(MutableSite site) {
		sites.add(site);
	}

	boolean hasSite(MutableSite site) {
		return sites.contains(site);
	}

	void makeListsUnmodifiable() {
		edges = Collections.unmodifiableList(edges);
		sites = Collections.unmodifiableList(sites);
	}
	
		

}
