package dev.mortus.voronoi.internal;

import dev.mortus.util.data.Pair;
import dev.mortus.voronoi.Vertex;
import dev.mortus.voronoi.internal.tree.Breakpoint;

public class HalfEdge extends MutableEdge {

	private HalfEdge twin;
	
	static Pair<HalfEdge> createTwinPair(Pair<Breakpoint> bps, Vertex vert) {
		if (bps.size() != 2) throw new RuntimeException("Cannot construct twin pair with a partial pair of breakpoints");
		
		HalfEdge edge = new HalfEdge(bps.first, vert);
		HalfEdge twin = new HalfEdge(bps.second, vert);
		
		edge.twin = twin;
		twin.twin = edge;
		
		Pair<HalfEdge> twins = new Pair<HalfEdge>(edge, edge.twin);
		return twins;
	}
	
	private HalfEdge(Breakpoint bp, Vertex start) {
		super(bp, start);
	}
	
	private HalfEdge(HalfEdge edge, Vertex end) {
		super(edge, end);
		this.twin = edge.twin;
		this.twin.twin = this;
	}
	
	HalfEdge getTwin() {
		return this.twin;
	}
	
	MutableEdge joinHalves() {
		return super.combine(this, this.twin);
	}

	@Override
	boolean isHalf() {
		return true;
	}
	
	HalfEdge finish(Vertex end) {
		return new HalfEdge(this, end);
	}
	
}
