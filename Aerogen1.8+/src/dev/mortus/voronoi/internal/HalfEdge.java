package dev.mortus.voronoi.internal;

import dev.mortus.voronoi.Edge;
import dev.mortus.voronoi.internal.tree.Breakpoint;

public class HalfEdge extends Edge {

	private HalfEdge twin;
	
	public HalfEdge(Breakpoint bp) {
		super(bp);
	}
	
	public void setTwin(HalfEdge twin) {
		if (twin != null) {
			this.twin = twin;
			twin.twin = this;
		}
	}
	
	public HalfEdge getTwin() {
		return this.twin;
	}

}
