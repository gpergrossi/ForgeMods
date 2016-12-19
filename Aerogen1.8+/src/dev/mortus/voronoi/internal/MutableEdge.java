package dev.mortus.voronoi.internal;

import dev.mortus.util.data.Pair;
import dev.mortus.voronoi.diagram.Edge;
import dev.mortus.voronoi.internal.tree.Breakpoint;

public class MutableEdge extends Edge {
	
	MutableEdge(MutableVertex start, MutableVertex end, MutableSite left, MutableSite right) {
		super(start, end, left, right);
	}
	
	// Start unfinished edge
	protected MutableEdge(Breakpoint bp, MutableVertex start) {
		super(start, null, bp.getArcRight().site, bp.getArcLeft().site);
	}
	
	// Finish unfinished edge
	protected MutableEdge(MutableEdge edge, MutableVertex end) {
		super(edge.getStart(), end, edge.sites.first, edge.sites.second);
	}
	
	// Clip mutable edge
	private MutableEdge(MutableEdge edge, MutableVertex start, MutableVertex end) {
		super(start, end, edge.sites.first, edge.sites.second);
	}

	// Combine half edges
	private MutableEdge(HalfEdge edge, HalfEdge twin) {
		super(twin.getEnd(), edge.getEnd(), edge.sites.first, edge.sites.second);
		if (edge.getEnd() == null) throw new RuntimeException("Cannot combine, edge has null end");
		if (twin.getEnd() == null) throw new RuntimeException("Cannot combine, twin has null end");
	}	
	
	MutableEdge combine(HalfEdge edge, HalfEdge twin) {
		return new MutableEdge(edge, twin);
	}
	
	MutableEdge clip(MutableVertex start, MutableVertex end) {
		return new MutableEdge(this, start, end);
	}
	
	MutableEdge finish(MutableVertex end) {
		return new MutableEdge(this, end);
	}
	
	boolean isHalf() {
		return false;
	}
	
	public boolean isFinished() {
		return vertices.size() == 2;
	}
	
	MutableSite getSiteLeft() {
		return (MutableSite) sites.first;
	}
	
	MutableSite getSiteRight() {
		return (MutableSite) sites.second;
	}
	
	@Override
	public MutableVertex getStart() {
		return (MutableVertex) vertices.first;
	}
	
	@Override
	public MutableVertex getEnd() {
		return (MutableVertex) vertices.second;
	}

	public Pair<MutableSite> getSites() {
		return new Pair<>(getSiteLeft(), getSiteRight());
	}

	public Pair<MutableVertex> getVertices() {
		return new Pair<>(getStart(), getEnd());
	}
	
}
