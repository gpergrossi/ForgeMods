package dev.mortus.voronoi.internal;

import dev.mortus.voronoi.Edge;
import dev.mortus.voronoi.Vertex;
import dev.mortus.voronoi.internal.tree.Breakpoint;

public class MutableEdge extends Edge {
	
	// Start unfinished edge
	protected MutableEdge(Breakpoint bp, Vertex start) {
		super(start, null, bp.getArcLeft().site, bp.getArcRight().site);
	}
	
	// Finish unfinished edge
	protected MutableEdge(MutableEdge edge, Vertex end) {
		super(edge.start, end, edge.siteLeft, edge.siteRight);
	}
	
	// Clip mutable edge
	private MutableEdge(MutableEdge edge, Vertex start, Vertex end) {
		super(start, end, edge.siteLeft, edge.siteRight);
	}

	// Combine half edges
	private MutableEdge(HalfEdge edge, HalfEdge twin) {
		super(twin.end, edge.end, edge.siteLeft, edge.siteRight);
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
	
	boolean isTwin() {
		return false;
	}
	
}
