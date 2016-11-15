package dev.mortus.voronoi.internal;

import dev.mortus.voronoi.Edge;
import dev.mortus.voronoi.Site;
import dev.mortus.voronoi.Vertex;
import dev.mortus.voronoi.internal.tree.Breakpoint;

public class MutableEdge extends Edge {
	
	MutableEdge(Vertex start, Vertex end, Site left, Site right) {
		super(start, end, left, right);
	}
	
	// Start unfinished edge
	protected MutableEdge(Breakpoint bp, Vertex start) {
		super(start, null, bp.getArcRight().site, bp.getArcLeft().site);
	}
	
	// Finish unfinished edge
	protected MutableEdge(MutableEdge edge, Vertex end) {
		super(edge.start(), end, edge.sites.first, edge.sites.second);
	}
	
	// Clip mutable edge
	private MutableEdge(MutableEdge edge, Vertex start, Vertex end) {
		super(start, end, edge.sites.first, edge.sites.second);
	}

	// Combine half edges
	private MutableEdge(HalfEdge edge, HalfEdge twin) {
		super(twin.end(), edge.end(), edge.sites.first, edge.sites.second);
		if (edge.end() == null) throw new RuntimeException("Cannot combine, edge has null end");
		if (twin.end() == null) throw new RuntimeException("Cannot combine, twin has null end");
	}
	
	MutableEdge combine(HalfEdge edge, HalfEdge twin) {
		return new MutableEdge(edge, twin);
	}
	
	MutableEdge clip(Vertex start, Vertex end) {
		return new MutableEdge(this, start, end);
	}
	
	MutableEdge finish(Vertex end) {
		return new MutableEdge(this, end);
	}
	
	boolean isHalf() {
		return false;
	}
	
	Site getSiteLeft() {
		return sites.first;
	}
	
	Site getSiteRight() {
		return sites.second;
	}
	
}
